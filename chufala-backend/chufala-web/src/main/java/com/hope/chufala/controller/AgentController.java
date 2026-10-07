package com.hope.chufala.controller;


import com.hope.chufala.common.util.ThreadLocalUtils;
import com.hope.chufala.common.constant.ResultCode;
import com.hope.chufala.infra.SseManager;
import com.hope.chufala.infra.TaskQueue;
import com.hope.chufala.model.dto.ChatRequest;
import com.hope.chufala.model.dto.UserPlanDTO;
import com.hope.chufala.model.entity.AiConversation;
import com.hope.chufala.model.entity.AiMessage;
import com.hope.chufala.model.entity.PlanHistory;
import com.hope.chufala.common.model.vo.Result;
import com.hope.chufala.model.entity.UploadedFile;
import com.hope.chufala.model.vo.TravelItineraryVO;
import com.hope.chufala.service.*;
import com.hope.chufala.agent.AgentService;
import com.hope.chufala.agent.AgentTask;
import com.hope.chufala.common.exception.ResourceNotFoundException;
import com.hope.chufala.agent.tool.*;
import io.jsonwebtoken.Claims;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.UrlResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import reactor.core.publisher.Flux;

import java.net.MalformedURLException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.RejectedExecutionException;

/**
 * AI 智能体接口。
 *
 * <p>是智能旅行助手的统一入口，覆盖三类能力：
 * <ol>
 *   <li>行程规划：提交异步任务（{@code /plan}）并通过 SSE 订阅进度（{@code /progress/{taskId}}）；</li>
 *   <li>智能对话：以 SSE 流式返回模型回复，支持携带图片附件（由豆包模型做多模态理解）；</li>
 *   <li>会话管理：历史会话与消息的查询、删除，以及图片上传。</li>
 * </ol>
 *
 * <p>模型选择上，默认使用 qwen；deepseek 仅对 VIP 用户开放。所有接口的用户身份
 * 均取自 ThreadLocal 中的 JWT claims。
 *
 * @author 谢光湘
 */
@Slf4j
@RestController
@RequestMapping("/agent")
public class AgentController {
    @Autowired
    private ApplicationContext applicationContext;
    @Resource
    private IPlanHistoryService planHistoryService;
    @Autowired
    private IAiConversationService aiConversationService;
    @Autowired
    private IUserService userService;
    @Autowired
    private IAttachmentService attachmentService;
    @Autowired
    private IAiService aiService;
    @Autowired
    private AgentService agentService;
    @Autowired
    private TaskQueue taskQueue;
    @Autowired
    private SseManager sseManager;
    @Resource(name="qwen")
    private ChatClient defaultChatClient;
    @Resource(name="doubao")
    private ChatClient doubaoChatClient;
    @Autowired
    private MessageChatMemoryAdvisor chatMemoryAdvisor;
    @Autowired
    private OrderTools orderTools;
    @Autowired
    private AttractionTools attractionTools;
    @Autowired
    private UserInfoTools userInfoTools;
    @Autowired
    private HotelInfoTools hotelInfoTools;
    @Autowired
    private TimeTools otherTools;

    /** 智能助手的系统提示词：限定旅游领域、规定 Markdown 输出格式与注意事项 */
    private static final String SYSTEM_PROMPT =
            """
                    # 角色：\s
                    - **身份**：旅游网站“出发啦”的AI旅行顾问。
                    - **主要工作内容**：提供旅游相关信息，制定行程规划，可帮助订购酒店（订单信息一定要用户先确认后再创建），可以查询当前时间，同时传递友好和温馨的服务态度。
          
                    # 工作流程/工作任务：\s
                    1. 分析用户问题，确定其是否与旅游相关。
                    2. 如果问题与旅游相关，提供针对性的旅游建议或信息，并融入人情味。
                    3. 如果问题与旅游无关，以幽默的方式拒绝回答，并引导用户回到旅游相关的话题。
            
                   #输出格式要求：\s
                   - 请使用 **纯 Markdown 语法** 组织回答，确保内容结构清晰、层级分明、美观且易于阅读。
                   - 使用 `##` 或 `###` 标题分隔逻辑段落（如“推荐景点”、“旅行小贴士”）。
                   - 列表项请用 `-` 或 `1.` 编写，便于阅读。
                   - 重要信息（如安全提醒、温馨建议）请用 `**加粗**` 强调。
                   - 如涉及代码、日期格式或命令示例，请用代码块（```）包裹。
                   - 避免大段无分段文字，每段不超过3-4行。
                   - 对于图片链接，请直接展示为 Markdown 图片语法：`![描述](图片链接)`。
                   - 建议使用一些小图标，如 `🌟`、`📌`，以增加回答的趣味性和视觉吸引力。
                   - 对于一些关键信息（例如：订单ID）,可以添加字体背景色以突出显示。
            
                    # 注意事项：\s
                    - 确保回答中始终融入人情味，传递友好和关怀的服务态度。
                    - 针对用户的具体需求提供定制化的旅游信息，并适当提醒旅行中的注意事项。
                    - 对于无关旅游的问题，以幽默的方式拒绝，并引导用户回到旅游相关的话题。
                    - 不要在回答中提及景点、酒店、房间的具体ID。
       
            """;


    /** 允许客户端指定的模型白名单，实际 Bean 名与之一致 */
    private static final Set<String> ALLOWED_MODELS = Set.of("deepseek", "qwen");

    private boolean isValidModel(String model) {
        return ALLOWED_MODELS.contains(model);
    }

    /**
     * 提交行程规划任务
     *
     * @param userPlanDTO 用户规划信息
     * @return 任务ID
     */
    @PostMapping("/plan")
    public Result submitTask(@RequestBody UserPlanDTO userPlanDTO) {
        System.out.println(userPlanDTO);
        Claims claims = ThreadLocalUtils.get();
        Long userId = claims.get("userId", Long.class);
        String taskId = taskQueue.submitTask(userId, userPlanDTO);
        try {
            agentService.planTravel(taskId,userId);    //根据任务ID 异步执行规划任务
        } catch (RejectedExecutionException e) {
            taskQueue.removeTask(taskId);
            log.warn("行程规划线程池已满，拒绝任务 taskId={}", taskId);
            return Result.fail(ResultCode.SYSTEM_BUSY);
        }
        return Result.ok(taskId);
    }

    /**
     * 订阅规划进度
     *
     * @param taskId 任务 ID
     * @return SSE 发射器，超时时间 5 分钟
     */
    @GetMapping("/progress/{taskId}")
    public SseEmitter subscribeProgress(@PathVariable String taskId){
        log.info("进度订阅---任务ID：{}", taskId);
        Claims claims = ThreadLocalUtils.get();
        Long userId = claims.get("userId", Long.class);
        AgentTask task = taskQueue.getTask(taskId);
        if (task == null || !userId.equals(task.getUserId())) {
            throw new ResourceNotFoundException("任务不存在");
        }
        SseEmitter emitter = new SseEmitter(300_000L);
        sseManager.registerEmitter(taskId, emitter);
        return emitter;
    }


    /**
     * 查询历史规划
     *
     * @return 当前用户的规划历史列表
     */
    @GetMapping("/plan/history")
    public Result history() {
        Claims claims = ThreadLocalUtils.get();
        Long userId = claims.get("userId", Long.class);
        List<PlanHistory> history = planHistoryService.queryHistory(userId);
        return Result.ok(history);
    }

    /**
     * 查询规划的结果
     *
     * @param id 历史记录 ID
     * @return 完整行程结果
     */
    @GetMapping("/plan/{id}")
    public Result queryPlanResult(@PathVariable Long id) {
        Claims claims = ThreadLocalUtils.get();
        Long userId = claims.get("userId", Long.class);
        TravelItineraryVO planResult = planHistoryService.queryPlanResult(id, userId);
        log.info("规划结果：{}", planResult);
        return Result.ok(planResult);
    }

    /**
     * 智能助手聊天
     *
     * @param chatRequest 对话请求（消息、会话、模型、关联规划、附件）
     * @return 以 text/event-stream 流式返回模型输出；响应头 X-Conversation-Id 回传会话 ID
     */
    @PostMapping(value = "/chat", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public ResponseEntity<Flux<String>> chat(@RequestBody ChatRequest chatRequest) {
        String conversationId = chatRequest.getConversationId();
        Claims claims = ThreadLocalUtils.get();
        Long userId = claims.get("userId", Long.class);
        String message = chatRequest.getMessage();
        String planId = chatRequest.getPlanId();
        List<String> fileIds = chatRequest.getFileIds();
        log.info("ChatRequest: userId={}, conversationId={}, planId={}, message={}, fileIds={}",
                userId, conversationId, planId, message, fileIds);
        String finalMessage = message;
        if (planId != null && !planId.isEmpty()) {
            // 如果有 planId，说明是和行程规划相关的聊天
            String plan = planHistoryService.getPlanContentById(Long.valueOf(planId), userId);
            finalMessage = "这是我的旅行行程规划内容：\n" + plan + "\n基于以上行程规划，" + message;
        }
        String modelName = chatRequest.getModel();
        //选择聊天模型
        ChatClient chatClient = defaultChatClient;
        if (isValidModel(modelName)) {
            if ("deepseek".equals(modelName) && !userService.isVipUser(userId)) {
                log.warn("{}是非VIP用户，不可使用 DeepSeek 模型", userId);
                return ResponseEntity.status(403).body(Flux.just("非VIP用户不可使用 DeepSeek 模型，请升级为VIP用户后重试。"));
            }
            chatClient = applicationContext.getBean(modelName, ChatClient.class);
        }
        Long messageId = null;
        if (conversationId == null || conversationId.isEmpty()) {
            //创建会话，并保存第一条用户消息
            Map<String, Long> result = aiConversationService.createConversationAndSaveFirstMessage(userId, message);
            conversationId = String.valueOf(result.get("conversationId"));
            messageId = result.get("messageId");
        } else {
            //已有会话ID，直接保存用户消息
            aiConversationService.requireConversationOwner(Long.valueOf(conversationId), userId);
            messageId = aiConversationService.saveMessage(conversationId, "user", message);
        }

        // 格式化系统提示，插入当前会话ID
        String finalConversationId = conversationId;
        String systemPrompt = SYSTEM_PROMPT;
        System.out.println(SYSTEM_PROMPT);

        // 创建助手消息草稿
        String assistantMessageId = aiConversationService.createAssistantMessageDraft(finalConversationId);
        StringBuilder contentBuilder = new StringBuilder();
        Flux<String> flux = null;

        if (fileIds != null && !fileIds.isEmpty()) {
            log.info("用户上传了图片，文件ID：{}", fileIds);
            // 如果有文件ID，说明用户上传了图片，获取图片URL并附加到消息中
            List<UploadedFile> files = aiService.getFilesByIds(fileIds, userId);
            for (UploadedFile file : files) {
                String fileUrl = file.getFileUrl();
                UrlResource resource = null;
                try {
                    resource = new UrlResource(fileUrl);
                } catch (MalformedURLException e) {
                    log.error("图片URL格式错误", e);
                    throw new RuntimeException("图片URL格式错误" + e);
                }
                String extension = fileUrl.substring(fileUrl.lastIndexOf(".") + 1).toLowerCase();
                MediaType mediaType = null;
                if (extension.equals("jpg") || extension.equals("jpeg")) {
                    mediaType = MediaType.IMAGE_JPEG;
                } else if (extension.equals("png")) {
                    mediaType = MediaType.IMAGE_PNG;
                } else if (extension.equals("gif")) {
                    mediaType = MediaType.IMAGE_GIF;
                } else {
                    mediaType = MediaType.APPLICATION_OCTET_STREAM;
                }
                //将文件保存到文件附加表中
                attachmentService.saveAttachmentInfo(messageId, file);

                MediaType finalMediaType = mediaType;
                UrlResource finalResource = resource;
                log.error("豆包图片分析调用开始，图片URL：{}", fileUrl);
                String finalMessage1 = finalMessage;
                flux = doubaoChatClient.prompt()
                        .system(systemPrompt)
                        .user(u -> u.text(finalMessage1).media(finalMediaType, finalResource))
                        .advisors(chatMemoryAdvisor)
                        .advisors(chatMemoryAdvisor -> chatMemoryAdvisor.param(ChatMemory.CONVERSATION_ID, finalConversationId))
                        .tools(orderTools, attractionTools, userInfoTools, hotelInfoTools, otherTools)
                        .toolContext(Map.of("userId", userId))
                        .stream()
                        .content()
                        .doOnNext(contentBuilder::append)
                        .doOnComplete(() -> {
                            // 流式输出结束，更新完整的消息内容
                            aiConversationService.updateAssistantMessageContent(assistantMessageId, contentBuilder.toString(), true);
                        })
                        .doOnError(e -> {
                            log.error("智能助手流式响应错误", e);
                            aiConversationService.updateAssistantMessageContent(assistantMessageId, contentBuilder.toString(), false);
                        });

                //  log.info("图片分析结果：{}", analysisResult);
                //将 图片分析结果 到 会话消息中
                //  aiConversationService.saveMessage(conversationId, "assistant", "图片内容分析结果："+analysisResult);
            }
        } else {

            flux = chatClient.prompt()
                    .system(systemPrompt)
                    .user(finalMessage)
                    .advisors(chatMemoryAdvisor)
                    .advisors(chatMemoryAdvisor -> chatMemoryAdvisor.param(ChatMemory.CONVERSATION_ID, finalConversationId))
                    .tools(orderTools, attractionTools, userInfoTools, hotelInfoTools, otherTools)
                    .toolContext(Map.of("userId", userId))
                    .stream()
                    .content()
                    .doOnNext(contentBuilder::append)
                    .doOnComplete(() -> {
                        // 流式输出结束，更新完整的消息内容
                        aiConversationService.updateAssistantMessageContent(assistantMessageId, contentBuilder.toString(), true);
                    })
                    .doOnError(e -> {
                        log.error("智能助手流式响应错误", e);
                        aiConversationService.updateAssistantMessageContent(assistantMessageId, contentBuilder.toString(), false);
                    });

        }
        return ResponseEntity.ok()
                .header("X-Conversation-Id", conversationId)
                .body(flux);
    }

    /**
     * 获取历史聊天会话
     *
     * @return 当前用户的会话列表
     */
    @GetMapping("/chat/history")
    public Result getChatHistory() {
        Claims claims = ThreadLocalUtils.get();
        Long userId = claims.get("userId", Long.class);
        List<AiConversation> conversations = aiConversationService.getConversationListByUserId(userId);
        return Result.ok(conversations);
    }


    /**
     * 查询指定会话下的全部消息。
     *
     * @param id 会话 ID
     * @return 消息列表
     */
    @GetMapping("/chat/{id}")
    public Result getChatById(@PathVariable Long id) {
        Claims claims = ThreadLocalUtils.get();
        Long userId = claims.get("userId", Long.class);
        List<AiMessage> conversation = aiConversationService.getConversationById(id, userId);
        return Result.ok(conversation);
    }

    /**
     * 删除指定会话。
     *
     * @param id 会话 ID
     * @return 操作结果
     */
    @DeleteMapping("/chat/{id}")
    public Result deleteChatById(@PathVariable Long id) {
        Claims claims = ThreadLocalUtils.get();
        Long userId = claims.get("userId", Long.class);
        aiConversationService.deleteConversationById(id, userId);
        return Result.ok(null);
    }

    /**
     * 上传对话图片附件。
     *
     * @param files 上传的图片文件
     * @return 文件 ID 列表
     */
    @PostMapping("/chat-image")
    public Result uploadChatImage(@RequestParam("files")MultipartFile[] files){
        Claims claims = ThreadLocalUtils.get();
        Long userId = claims.get("userId", Long.class);
        List<String> fileIds = aiService.uploadChatFile(files, userId);
        log.info("上传图片成功，文件ID：{}", fileIds);
        return Result.ok(fileIds);
    }
}
//        UrlResource resource = null;
//        try {
//            resource = new UrlResource("https://chufala.oss-cn-shenzhen.aliyuncs.com/ec165819-b89a-4f8c-8c28-c6b42a400c8a.jpg");
//        } catch (MalformedURLException e) {
//            log.error("图片URL格式错误", e);
//            throw new RuntimeException("无效的图片URL", e);
//        }
