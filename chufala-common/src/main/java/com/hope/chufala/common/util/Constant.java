package com.hope.chufala.common.util;

public class Constant {
    public static final String html ="\n" +
            "<!DOCTYPE html>\n" +
            "<html lang=\"zh-CN\">\n" +
            "<head>\n" +
            "    <meta charset=\"UTF-8\">\n" +
            "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n" +
            "    <title>吉首大学到张家界2日游行程规划</title>\n" +
            "    <link rel=\"stylesheet\" href=\"https://lf6-cdn-tos.bytecdntp.com/cdn/expire-100-M/font-awesome/6.0.0/css/all.min.css\">\n" +
            "    <link rel=\"stylesheet\" href=\"https://lf3-cdn-tos.bytecdntp.com/cdn/expire-1-M/tailwindcss/2.2.19/tailwind.min.css\">\n" +
            "    <link rel=\"stylesheet\" href=\"https://fonts.googleapis.com/css2?family=Noto+Serif+SC:wght@400;500;600;700&family=Noto+Sans+SC:wght@300;400;500;700&display=swap\">\n" +
            "    <link rel=\"stylesheet\" href=\"https://unpkg.com/leaflet@1.9.4/dist/leaflet.css\" integrity=\"sha256-p4NxAoJBhIIN+hmNHrzRCf9tD/miZyoHS5obTRR9BMY=\" crossorigin=\"\"/>\n" +
            "    <script src=\"https://unpkg.com/leaflet@1.9.4/dist/leaflet.js\" integrity=\"sha256-20nQCchB9co0qIjJZRGuk2/Z9VM+kNiyxNV1lvTlZBo=\" crossorigin=\"\"></script>\n" +
            "    <script src=\"https://cdn.jsdelivr.net/npm/chart.js\"></script>\n" +
            "    <style>\n" +
            "        :root {\n" +
            "            --primary: #3b82f6;\n" +
            "            --secondary: #10b981;\n" +
            "            --accent: #f59e0b;\n" +
            "            --text: #1f2937;\n" +
            "            --bg: #f8fafc;\n" +
            "            --card-bg: #ffffff;\n" +
            "        }\n" +
            "        \n" +
            "        body {\n" +
            "            font-family: 'Noto Sans SC', sans-serif;\n" +
            "            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);\n" +
            "            min-height: 100vh;\n" +
            "            color: var(--text);\n" +
            "        }\n" +
            "        \n" +
            "        .container {\n" +
            "            max-width: 1200px;\n" +
            "            margin: 0 auto;\n" +
            "            padding: 20px;\n" +
            "        }\n" +
            "        \n" +
            "        .card {\n" +
            "            background: var(--card-bg);\n" +
            "            border-radius: 20px;\n" +
            "            box-shadow: 0 20px 40px rgba(0,0,0,0.1);\n" +
            "            margin-bottom: 30px;\n" +
            "            overflow: hidden;\n" +
            "            transition: transform 0.3s ease;\n" +
            "        }\n" +
            "        \n" +
            "        .card:hover {\n" +
            "            transform: translateY(-5px);\n" +
            "        }\n" +
            "        \n" +
            "        .card-header {\n" +
            "            background: linear-gradient(135deg, var(--primary), var(--secondary));\n" +
            "            color: white;\n" +
            "            padding: 25px;\n" +
            "            border-bottom: 1px solid #e5e7eb;\n" +
            "        }\n" +
            "        \n" +
            "        .card-body {\n" +
            "            padding: 25px;\n" +
            "        }\n" +
            "        \n" +
            "        .hero-section {\n" +
            "            background: linear-gradient(rgba(0,0,0,0.5), rgba(0,0,0,0.5)), url('https://images.unsplash.com/photo-1544551763-46a013bb70d5?ixlib=rb-4.0.3&auto=format&fit=crop&w=1200&q=80');\n" +
            "            background-size: cover;\n" +
            "            background-position: center;\n" +
            "            color: white;\n" +
            "            text-align: center;\n" +
            "            padding: 80px 20px;\n" +
            "            border-radius: 20px;\n" +
            "            margin-bottom: 30px;\n" +
            "        }\n" +
            "        \n" +
            "        .timeline {\n" +
            "            position: relative;\n" +
            "            padding-left: 30px;\n" +
            "        }\n" +
            "        \n" +
            "        .timeline::before {\n" +
            "            content: '';\n" +
            "            position: absolute;\n" +
            "            left: 15px;\n" +
            "            top: 0;\n" +
            "            bottom: 0;\n" +
            "            width: 2px;\n" +
            "            background: var(--primary);\n" +
            "        }\n" +
            "        \n" +
            "        .timeline-item {\n" +
            "            position: relative;\n" +
            "            margin-bottom: 30px;\n" +
            "        }\n" +
            "        \n" +
            "        .timeline-item::before {\n" +
            "            content: '';\n" +
            "            position: absolute;\n" +
            "            left: -23px;\n" +
            "            top: 5px;\n" +
            "            width: 12px;\n" +
            "            height: 12px;\n" +
            "            border-radius: 50%;\n" +
            "            background: var(--secondary);\n" +
            "            border: 3px solid white;\n" +
            "            box-shadow: 0 0 0 3px var(--secondary);\n" +
            "        }\n" +
            "        \n" +
            "        .icon-box {\n" +
            "            display: inline-flex;\n" +
            "            align-items: center;\n" +
            "            justify-content: center;\n" +
            "            width: 50px;\n" +
            "            height: 50px;\n" +
            "            border-radius: 12px;\n" +
            "            margin-right: 15px;\n" +
            "            color: white;\n" +
            "        }\n" +
            "        \n" +
            "        .transport-icon {\n" +
            "            background: var(--primary);\n" +
            "        }\n" +
            "        \n" +
            "        .food-icon {\n" +
            "            background: var(--accent);\n" +
            "        }\n" +
            "        \n" +
            "        .attraction-icon {\n" +
            "            background: var(--secondary);\n" +
            "        }\n" +
            "        \n" +
            "        .hotel-icon {\n" +
            "            background: #8b5cf6;\n" +
            "        }\n" +
            "        \n" +
            "        #map {\n" +
            "            height: 400px;\n" +
            "            border-radius: 15px;\n" +
            "            margin-top: 20px;\n" +
            "        }\n" +
            "        \n" +
            "        .budget-chart {\n" +
            "            max-width: 300px;\n" +
            "            margin: 0 auto;\n" +
            "        }\n" +
            "        \n" +
            "        .location-link {\n" +
            "            color: var(--primary);\n" +
            "            text-decoration: none;\n" +
            "            font-weight: 500;\n" +
            "            cursor: pointer;\n" +
            "        }\n" +
            "        \n" +
            "        .location-link:hover {\n" +
            "            text-decoration: underline;\n" +
            "        }\n" +
            "        \n" +
            "        .weather-card {\n" +
            "            background: linear-gradient(135deg, #74b9ff, #0984e3);\n" +
            "            color: white;\n" +
            "        }\n" +
            "        \n" +
            "        @media (max-width: 768px) {\n" +
            "            .container {\n" +
            "                padding: 10px;\n" +
            "            }\n" +
            "            \n" +
            "            .hero-section {\n" +
            "                padding: 60px 15px;\n" +
            "            }\n" +
            "            \n" +
            "            .card-body {\n" +
            "                padding: 20px;\n" +
            "            }\n" +
            "        }\n" +
            "    </style>\n" +
            "</head>\n" +
            "<body>\n" +
            "    <div class=\"container\">\n" +
            "        <!-- Hero Section -->\n" +
            "        <div class=\"hero-section\">\n" +
            "            <h1 class=\"text-4xl md:text-6xl font-bold mb-4\">吉首大学 → 张家界</h1>\n" +
            "            <p class=\"text-xl md:text-2xl opacity-90\">6人团队 · 2天深度游 · 公共交通出行</p>\n" +
            "            <div class=\"mt-6 flex flex-wrap justify-center gap-4\">\n" +
            "                <span class=\"bg-white bg-opacity-20 px-4 py-2 rounded-full\"><i class=\"fas fa-calendar-alt mr-2\"></i>2天行程</span>\n" +
            "                <span class=\"bg-white bg-opacity-20 px-4 py-2 rounded-full\"><i class=\"fas fa-users mr-2\"></i>6人团队</span>\n" +
            "                <span class=\"bg-white bg-opacity-20 px-4 py-2 rounded-full\"><i class=\"fas fa-bus mr-2\"></i>公共交通</span>\n" +
            "            </div>\n" +
            "        </div>\n" +
            "\n" +
            "        <!-- 天气信息 -->\n" +
            "        <div class=\"card weather-card\">\n" +
            "            <div class=\"card-body\">\n" +
            "                <h2 class=\"text-2xl font-bold mb-4\"><i class=\"fas fa-cloud-sun mr-3\"></i>天气信息</h2>\n" +
            "                <div class=\"grid grid-cols-2 md:grid-cols-4 gap-4\">\n" +
            "                    <div class=\"text-center\">\n" +
            "                        <div class=\"text-lg font-semibold\">第1天</div>\n" +
            "                        <div class=\"text-3xl\">\uD83C\uDF24\uFE0F</div>\n" +
            "                        <div>32°C / 21°C</div>\n" +
            "                        <div class=\"text-sm\">晴转小雨</div>\n" +
            "                    </div>\n" +
            "                    <div class=\"text-center\">\n" +
            "                        <div class=\"text-lg font-semibold\">第2天</div>\n" +
            "                        <div class=\"text-3xl\">\uD83C\uDF27\uFE0F</div>\n" +
            "                        <div>29°C / 22°C</div>\n" +
            "                        <div class=\"text-sm\">小雨</div>\n" +
            "                    </div>\n" +
            "                </div>\n" +
            "                <div class=\"mt-4 text-sm opacity-90\">\n" +
            "                    <i class=\"fas fa-info-circle mr-2\"></i>建议携带雨具，穿着舒适的运动鞋\n" +
            "                </div>\n" +
            "            </div>\n" +
            "        </div>\n" +
            "\n" +
            "        <!-- 行程概览 -->\n" +
            "        <div class=\"card\">\n" +
            "            <div class=\"card-header\">\n" +
            "                <h2 class=\"text-2xl font-bold\"><i class=\"fas fa-map-marked-alt mr-3\"></i>行程概览</h2>\n" +
            "            </div>\n" +
            "            <div class=\"card-body\">\n" +
            "                <div class=\"grid grid-cols-1 md:grid-cols-2 gap-6\">\n" +
            "                    <div>\n" +
            "                        <h3 class=\"text-xl font-semibold mb-3 text-blue-600\">第一天：抵达与初探</h3>\n" +
            "                        <ul class=\"space-y-2\">\n" +
            "                            <li><i class=\"fas fa-bus text-blue-500 mr-2\"></i>吉首大学 → 吉首东站</li>\n" +
            "                            <li><i class=\"fas fa-train text-green-500 mr-2\"></i>高铁前往张家界西站</li>\n" +
            "                            <li><i class=\"fas fa-mountain text-purple-500 mr-2\"></i>张家界国家森林公园</li>\n" +
            "                            <li><i class=\"fas fa-utensils text-orange-500 mr-2\"></i>品尝当地特色美食</li>\n" +
            "                        </ul>\n" +
            "                    </div>\n" +
            "                    <div>\n" +
            "                        <h3 class=\"text-xl font-semibold mb-3 text-green-600\">第二天：深度游览</h3>\n" +
            "                        <ul class=\"space-y-2\">\n" +
            "                            <li><i class=\"fas fa-hiking text-green-500 mr-2\"></i>金鞭溪徒步</li>\n" +
            "                            <li><i class=\"fas fa-cable-car text-purple-500 mr-2\"></i>百龙天梯体验</li>\n" +
            "                            <li><i class=\"fas fa-landmark text-yellow-500 mr-2\"></i>袁家界景区</li>\n" +
            "                            <li><i class=\"fas fa-train text-blue-500 mr-2\"></i>返程回吉首</li>\n" +
            "                        </ul>\n" +
            "                    </div>\n" +
            "                </div>\n" +
            "            </div>\n" +
            "        </div>\n" +
            "\n" +
            "        <!-- 详细行程 -->\n" +
            "        <div class=\"card\">\n" +
            "            <div class=\"card-header\">\n" +
            "                <h2 class=\"text-2xl font-bold\"><i class=\"fas fa-clock mr-3\"></i>详细时间表</h2>\n" +
            "            </div>\n" +
            "            <div class=\"card-body\">\n" +
            "                <div class=\"timeline\">\n" +
            "                    <!-- 第一天 -->\n" +
            "                    <div class=\"timeline-item\">\n" +
            "                        <h3 class=\"text-xl font-semibold text-blue-600 mb-2\">第一天：9月28日（周日）</h3>\n" +
            "                        <div class=\"bg-gray-50 p-4 rounded-lg\">\n" +
            "                            <div class=\"flex items-center mb-3\">\n" +
            "                                <div class=\"icon-box transport-icon\">\n" +
            "                                    <i class=\"fas fa-bus\"></i>\n" +
            "                                </div>\n" +
            "                                <div>\n" +
            "                                    <h4 class=\"font-semibold\">07:30-08:00 · 前往吉首东站</h4>\n" +
            "                                    <p>从吉首大学乘坐22路公交车前往吉首东站</p>\n" +
            "                                    <p class=\"text-sm text-gray-600\">票价：2元/人 · 时长：约30分钟</p>\n" +
            "                                </div>\n" +
            "                            </div>\n" +
            "                            \n" +
            "                            <div class=\"flex items-center mb-3\">\n" +
            "                                <div class=\"icon-box transport-icon\">\n" +
            "                                    <i class=\"fas fa-train\"></i>\n" +
            "                                </div>\n" +
            "                                <div>\n" +
            "                                    <h4 class=\"font-semibold\">08:30-09:30 · 高铁前往张家界</h4>\n" +
            "                                    <p>乘坐G688次高铁从吉首东站到张家界西站</p>\n" +
            "                                    <p class=\"text-sm text-gray-600\">票价：约35元/人 · 时长：约1小时</p>\n" +
            "                                </div>\n" +
            "                            </div>\n" +
            "                            \n" +
            "                            <div class=\"flex items-center mb-3\">\n" +
            "                                <div class=\"icon-box transport-icon\">\n" +
            "                                    <i class=\"fas fa-bus\"></i>\n" +
            "                                </div>\n" +
            "                                <div>\n" +
            "                                    <h4 class=\"font-semibold\">09:45-10:30 · 前往景区</h4>\n" +
            "                                    <p>从张家界西站乘坐16路转11路公交车前往张家界国家森林公园</p>\n" +
            "                                    <p class=\"text-sm text-gray-600\">票价：4元/人 · 时长：约45分钟</p>\n" +
            "                                </div>\n" +
            "                            </div>\n" +
            "                            \n" +
            "                            <div class=\"flex items-center mb-3\">\n" +
            "                                <div class=\"icon-box attraction-icon\">\n" +
            "                                    <i class=\"fas fa-mountain\"></i>\n" +
            "                                </div>\n" +
            "                                <div>\n" +
            "                                    <h4 class=\"font-semibold\">10:30-17:00 · 张家界国家森林公园</h4>\n" +
            "                                    <p>游览金鞭溪、黄石寨等核心景点</p>\n" +
            "                                    <p class=\"text-sm text-gray-600\">门票：228元/人（4日有效）· 建议游览时间：6小时</p>\n" +
            "                                </div>\n" +
            "                            </div>\n" +
            "                            \n" +
            "                            <div class=\"flex items-center\">\n" +
            "                                <div class=\"icon-box food-icon\">\n" +
            "                                    <i class=\"fas fa-utensils\"></i>\n" +
            "                                </div>\n" +
            "                                <div>\n" +
            "                                    <h4 class=\"font-semibold\">18:00-19:30 · 晚餐</h4>\n" +
            "                                    <p>在景区附近品尝当地特色土家菜</p>\n" +
            "                                    <p class=\"text-sm text-gray-600\">人均消费：60-80元</p>\n" +
            "                                </div>\n" +
            "                            </div>\n" +
            "                        </div>\n" +
            "                    </div>\n" +
            "                    \n" +
            "                    <!-- 第二天 -->\n" +
            "                    <div class=\"timeline-item\">\n" +
            "                        <h3 class=\"text-xl font-semibold text-green-600 mb-2\">第二天：9月29日（周一）</h3>\n" +
            "                        <div class=\"bg-gray-50 p-4 rounded-lg\">\n" +
            "                            <div class=\"flex items-center mb-3\">\n" +
            "                                <div class=\"icon-box attraction-icon\">\n" +
            "                                    <i class=\"fas fa-hiking\"></i>\n" +
            "                                </div>\n" +
            "                                <div>\n" +
            "                                    <h4 class=\"font-semibold\">08:00-12:00 · 袁家界景区</h4>\n" +
            "                                    <p>游览天下第一桥、哈利路亚山等景点</p>\n" +
            "                                    <p class=\"text-sm text-gray-600\">包含在门票内 · 建议游览时间：4小时</p>\n" +
            "                                </div>\n" +
            "                            </div>\n" +
            "                            \n" +
            "                            <div class=\"flex items-center mb-3\">\n" +
            "                                <div class=\"icon-box attraction-icon\">\n" +
            "                                    <i class=\"fas fa-cable-car\"></i>\n" +
            "                                </div>\n" +
            "                                <div>\n" +
            "                                    <h4 class=\"font-semibold\">12:30-13:30 · 百龙天梯</h4>\n" +
            "                                    <p>体验世界最高户外电梯，欣赏壮丽景色</p>\n" +
            "                                    <p class=\"text-sm text-gray-600\">票价：65元/人（单程）</p>\n" +
            "                                </div>\n" +
            "                            </div>\n" +
            "                            \n" +
            "                            <div class=\"flex items-center mb-3\">\n" +
            "                                <div class=\"icon-box food-icon\">\n" +
            "                                    <i class=\"fas fa-utensils\"></i>\n" +
            "                                </div>\n" +
            "                                <div>\n" +
            "                                    <h4 class=\"font-semibold\">13:30-14:30 · 午餐</h4>\n" +
            "                                    <p>在景区内用餐，品尝当地小吃</p>\n" +
            "                                    <p class=\"text-sm text-gray-600\">人均消费：40-60元</p>\n" +
            "                                </div>\n" +
            "                            </div>\n" +
            "                            \n" +
            "                            <div class=\"flex items-center mb-3\">\n" +
            "                                <div class=\"icon-box transport-icon\">\n" +
            "                                    <i class=\"fas fa-bus\"></i>\n" +
            "                                </div>\n" +
            "                                <div>\n" +
            "                                    <h4 class=\"font-semibold\">15:00-16:00 · 返回市区</h4>\n" +
            "                                    <p>乘坐景区专线车返回张家界市区</p>\n" +
            "                                    <p class=\"text-sm text-gray-600\">票价：15元/人 · 时长：约1小时</p>\n" +
            "                                </div>\n" +
            "                            </div>\n" +
            "                            \n" +
            "                            <div class=\"flex items-center\">\n" +
            "                                <div class=\"icon-box transport-icon\">\n" +
            "                                    <i class=\"fas fa-train\"></i>\n" +
            "                                </div>\n" +
            "                                <div>\n" +
            "                                    <h4 class=\"font-semibold\">16:30-18:30 · 返回吉首</h4>\n" +
            "                                    <p>乘坐高铁返回吉首，结束愉快旅程</p>\n" +
            "                                    <p class=\"text-sm text-gray-600\">票价：约35元/人 · 时长：约1小时</p>\n" +
            "                                </div>\n" +
            "                            </div>\n" +
            "                        </div>\n" +
            "                    </div>\n" +
            "                </div>\n" +
            "            </div>\n" +
            "        </div>\n" +
            "\n" +
            "        <!-- 交通信息 -->\n" +
            "        <div class=\"card\">\n" +
            "            <div class=\"card-header\">\n" +
            "                <h2 class=\"text-2xl font-bold\"><i class=\"fas fa-route mr-3\"></i>交通信息</h2>\n" +
            "            </div>\n" +
            "            <div class=\"card-body\">\n" +
            "                <div class=\"grid grid-cols-1 md:grid-cols-2 gap-6\">\n" +
            "                    <div>\n" +
            "                        <h3 class=\"text-lg font-semibold mb-3\">去程路线</h3>\n" +
            "                        <div class=\"space-y-2\">\n" +
            "                            <div class=\"flex items-center\">\n" +
            "                                <div class=\"w-6 h-6 bg-blue-500 rounded-full flex items-center justify-center text-white text-sm mr-3\">1</div>\n" +
            "                                <span>吉首大学 → 吉首东站（22路公交）</span>\n" +
            "                            </div>\n" +
            "                            <div class=\"flex items-center\">\n" +
            "                                <div class=\"w-6 h-6 bg-blue-500 rounded-full flex items-center justify-center text-white text-sm mr-3\">2</div>\n" +
            "                                <span>吉首东站 → 张家界西站（G688高铁）</span>\n" +
            "                            </div>\n" +
            "                            <div class=\"flex items-center\">\n" +
            "                                <div class=\"w-6 h-6 bg-blue-500 rounded-full flex items-center justify-center text-white text-sm mr-3\">3</div>\n" +
            "                                <span>张家界西站 → 景区（16路转11路）</span>\n" +
            "                            </div>\n" +
            "                        </div>\n" +
            "                    </div>\n" +
            "                    <div>\n" +
            "                        <h3 class=\"text-lg font-semibold mb-3\">返程路线</h3>\n" +
            "                        <div class=\"space-y-2\">\n" +
            "                            <div class=\"flex items-center\">\n" +
            "                                <div class=\"w-6 h-6 bg-green-500 rounded-full flex items-center justify-center text-white text-sm mr-3\">1</div>\n" +
            "                                <span>景区 → 张家界市区（专线车）</span>\n" +
            "                            </div>\n" +
            "                            <div class=\"flex items-center\">\n" +
            "                                <div class=\"w-6 h-6 bg-green-500 rounded-full flex items-center justify-center text-white text-sm mr-3\">2</div>\n" +
            "                                <span>张家界西站 → 吉首东站（高铁）</span>\n" +
            "                            </div>\n" +
            "                            <div class=\"flex items-center\">\n" +
            "                                <div class=\"w-6 h-6 bg-green-500 rounded-full flex items-center justify-center text-white text-sm mr-3\">3</div>\n" +
            "                                <span>吉首东站 → 吉首大学（公交）</span>\n" +
            "                            </div>\n" +
            "                        </div>\n" +
            "                    </div>\n" +
            "                </div>\n" +
            "                <div class=\"mt-6 p-4 bg-blue-50 rounded-lg\">\n" +
            "                    <h4 class=\"font-semibold text-blue-800 mb-2\"><i class=\"fas fa-lightbulb mr-2\"></i>交通提示</h4>\n" +
            "                    <ul class=\"text-sm text-blue-700 space-y-1\">\n" +
            "                        <li>• 建议提前购买高铁票，节假日需提前预订</li>\n" +
            "                        <li>• 景区内交通车包含在门票内，可无限次乘坐</li>\n" +
            "                        <li>• 保存好公交卡或准备零钱，部分线路不支持扫码</li>\n" +
            "                    </ul>\n" +
            "                </div>\n" +
            "            </div>\n" +
            "        </div>\n" +
            "\n" +
            "        <!-- 住宿与餐饮 -->\n" +
            "        <div class=\"card\">\n" +
            "            <div class=\"card-header\">\n" +
            "                <h2 class=\"text-2xl font-bold\"><i class=\"fas fa-hotel mr-3\"></i>住宿与餐饮推荐</h2>\n" +
            "            </div>\n" +
            "            <div class=\"card-body\">\n" +
            "                <div class=\"grid grid-cols-1 md:grid-cols-2 gap-6\">\n" +
            "                    <div>\n" +
            "                        <h3 class=\"text-lg font-semibold mb-3\"><i class=\"fas fa-bed text-purple-500 mr-2\"></i>住宿推荐</h3>\n" +
            "                        <div class=\"space-y-4\">\n" +
            "                            <div class=\"p-3 bg-gray-50 rounded-lg\">\n" +
            "                                <h4 class=\"font-semibold\">张家界国际大酒店</h4>\n" +
            "                                <p class=\"text-sm text-gray-600\">子午路三角坪巷145号</p>\n" +
            "                                <p class=\"text-sm\">参考价格：300-500元/晚</p>\n" +
            "                            </div>\n" +
            "                            <div class=\"p-3 bg-gray-50 rounded-lg\">\n" +
            "                                <h4 class=\"font-semibold\">张家界半岛大酒店</h4>\n" +
            "                                <p class=\"text-sm text-gray-600\">教场路19号</p>\n" +
            "                                <p class=\"text-sm\">参考价格：250-400元/晚</p>\n" +
            "                            </div>\n" +
            "                        </div>\n" +
            "                    </div>\n" +
            "                    <div>\n" +
            "                        <h3 class=\"text-lg font-semibold mb-3\"><i class=\"fas fa-utensils text-orange-500 mr-2\"></i>餐饮推荐</h3>\n" +
            "                        <div class=\"space-y-4\">\n" +
            "                            <div class=\"p-3 bg-gray-50 rounded-lg\">\n" +
            "                                <h4 class=\"font-semibold\">张家界筷乐湘西</h4>\n" +
            "                                <p class=\"text-sm text-gray-600\">军地坪画卷路魅力湘西文化广场</p>\n" +
            "                                <p class=\"text-sm\">特色：土家三下锅、血豆腐</p>\n" +
            "                            </div>\n" +
            "                            <div class=\"p-3 bg-gray-50 rounded-lg\">\n" +
            "                                <h4 class=\"font-semibold\">亲青菜食尚餐厅</h4>\n" +
            "                                <p class=\"text-sm text-gray-600\">教场路142号</p>\n" +
            "                                <p class=\"text-sm\">特色：湘西特色菜、环境优雅</p>\n" +
            "                            </div>\n" +
            "                        </div>\n" +
            "                    </div>\n" +
            "                </div>\n" +
            "            </div>\n" +
            "        </div>\n" +
            "\n" +
            "        <!-- 景点地图 -->\n" +
            "        <div class=\"card\">\n" +
            "            <div class=\"card-header\">\n" +
            "                <h2 class=\"text-2xl font-bold\"><i class=\"fas fa-map mr-3\"></i>景点分布地图</h2>\n" +
            "            </div>\n" +
            "            <div class=\"card-body\">\n" +
            "                <div id=\"map\"></div>\n" +
            "            </div>\n" +
            "        </div>\n" +
            "\n" +
            "        <!-- 预算分析 -->\n" +
            "        <div class=\"card\">\n" +
            "            <div class=\"card-header\">\n" +
            "                <h2 class=\"text-2xl font-bold\"><i class=\"fas fa-chart-pie mr-3\"></i>预算分析（6人总计）</h2>\n" +
            "            </div>\n" +
            "            <div class=\"card-body\">\n" +
            "                <div class=\"grid grid-cols-1 md:grid-cols-2 gap-6\">\n" +
            "                    <div class=\"budget-chart\">\n" +
            "                        <canvas id=\"budgetChart\"></canvas>\n" +
            "                    </div>\n" +
            "                    <div>\n" +
            "                        <div class=\"space-y-3\">\n" +
            "                            <div class=\"flex justify-between items-center\">\n" +
            "                                <span>交通费用</span>\n" +
            "                                <span class=\"font-semibold\">¥1,200</span>\n" +
            "                            </div>\n" +
            "                            <div class=\"flex justify-between items-center\">\n" +
            "                                <span>门票费用</span>\n" +
            "                                <span class=\"font-semibold\">¥1,368</span>\n" +
            "                            </div>\n" +
            "                            <div class=\"flex justify-between items-center\">\n" +
            "                                <span>住宿费用</span>\n" +
            "                                <span class=\"font-semibold\">¥1,200</span>\n" +
            "                            </div>\n" +
            "                            <div class=\"flex justify-between items-center\">\n" +
            "                                <span>餐饮费用</span>\n" +
            "                                <span class=\"font-semibold\">¥840</span>\n" +
            "                            </div>\n" +
            "                            <div class=\"flex justify-between items-center border-t pt-2\">\n" +
            "                                <span class=\"font-semibold\">总计</span>\n" +
            "                                <span class=\"font-semibold text-blue-600\">¥4,608</span>\n" +
            "                            </div>\n" +
            "                            <div class=\"flex justify-between items-center\">\n" +
            "                                <span>人均费用</span>\n" +
            "                                <span class=\"font-semibold text-green-600\">¥768</span>\n" +
            "                            </div>\n" +
            "                        </div>\n" +
            "                    </div>\n" +
            "                </div>\n" +
            "            </div>\n" +
            "        </div>\n" +
            "\n" +
            "        <!-- 实用信息 -->\n" +
            "        <div class=\"card\">\n" +
            "            <div class=\"card-header\">\n" +
            "                <h2 class=\"text-2xl font-bold\"><i class=\"fas fa-info-circle mr-3\"></i>实用信息</h2>\n" +
            "            </div>\n" +
            "            <div class=\"card-body\">\n" +
            "                <div class=\"grid grid-cols-1 md:grid-cols-2 gap-6\">\n" +
            "                    <div>\n" +
            "                        <h3 class=\"text-lg font-semibold mb-3\">紧急联系电话</h3>\n" +
            "                        <ul class=\"space-y-2\">\n" +
            "                            <li><i class=\"fas fa-phone text-red-500 mr-2\"></i>紧急救援：110</li>\n" +
            "                            <li><i class=\"fas fa-ambulance text-red-500 mr-2\"></i>医疗急救：120</li>\n" +
            "                            <li><i class=\"fas fa-fire-extinguisher text-red-500 mr-2\"></i>火警：119</li>\n" +
            "                            <li><i class=\"fas fa-map-marker-alt text-blue-500 mr-2\"></i>景区服务：0744-5611111</li>\n" +
            "                        </ul>\n" +
            "                    </div>\n" +
            "                    <div>\n" +
            "                        <h3 class=\"text-lg font-semibold mb-3\">重要提示</h3>\n" +
            "                        <ul class=\"space-y-2 text-sm\">\n" +
            "                            <li><i class=\"fas fa-hiking text-green-500 mr-2\"></i>穿着舒适的运动鞋，准备雨具</li>\n" +
            "                            <li><i class=\"fas fa-camera text-purple-500 mr-2\"></i>携带充电宝，景区内充电不便</li>\n" +
            "                            <li><i class=\"fas fa-utensils text-orange-500 mr-2\"></i>景区内餐饮较贵，可自带零食</li>\n" +
            "                            <li><i class=\"fas fa-sun text-yellow-500 mr-2\"></i>注意防晒，携带防晒用品</li>\n" +
            "                        </ul>\n" +
            "                    </div>\n" +
            "                </div>\n" +
            "                \n" +
            "                <div class=\"mt-6 p-4 bg-yellow-50 rounded-lg\">\n" +
            "                    <h4 class=\"font-semibold text-yellow-800 mb-2\"><i class=\"fas fa-exclamation-triangle mr-2\"></i>行李清单提醒</h4>\n" +
            "                    <div class=\"grid grid-cols-2 md:grid-cols-4 gap-4 text-sm\">\n" +
            "                        <div>\n" +
            "                            <div class=\"font-semibold\">证件类</div>\n" +
            "                            <div>身份证、学生证</div>\n" +
            "                        </div>\n" +
            "                        <div>\n" +
            "                            <div class=\"font-semibold\">衣物类</div>\n" +
            "                            <div>运动鞋、雨衣、外套</div>\n" +
            "                        </div>\n" +
            "                        <div>\n" +
            "                            <div class=\"font-semibold\">电子类</div>\n" +
            "                            <div>手机、充电宝、相机</div>\n" +
            "                        </div>\n" +
            "                        <div>\n" +
            "                            <div class=\"font-semibold\">其他</div>\n" +
            "                            <div>现金、药品、防晒霜</div>\n" +
            "                        </div>\n" +
            "                    </div>\n" +
            "                </div>\n" +
            "            </div>\n" +
            "        </div>\n" +
            "    </div>\n" +
            "\n" +
            "    <script>\n" +
            "        // 初始化地图\n" +
            "        function initMap() {\n" +
            "            const map = L.map('map').setView([29.353896, 110.469450], 11);\n" +
            "            \n" +
            "            // 使用高德地图瓦片\n" +
            "            L.tileLayer('https://webrd0{s}.is.autonavi.com/appmaptile?lang=zh_cn&size=1&scale=1&style=8&x={x}&y={y}&z={z}', {\n" +
            "                subdomains: ['1', '2', '3', '4'],\n" +
            "                attribution: '&copy; <a href=\"https://ditu.amap.com/\">高德地图</a>'\n" +
            "            }).addTo(map);\n" +
            "            \n" +
            "            // 添加景点标记\n" +
            "            const attractions = [\n" +
            "                {\n" +
            "                    name: \"张家界国家森林公园\",\n" +
            "                    lat: 29.353896,\n" +
            "                    lng: 110.469450,\n" +
            "                    type: \"main\"\n" +
            "                },\n" +
            "                {\n" +
            "                    name: \"金鞭溪\",\n" +
            "                    lat: 29.345678,\n" +
            "                    lng: 110.456789,\n" +
            "                    type: \"attraction\"\n" +
            "                },\n" +
            "                {\n" +
            "                    name: \"袁家界\",\n" +
            "                    lat: 29.367890,\n" +
            "                    lng: 110.478901,\n" +
            "                    type: \"attraction\"\n" +
            "                },\n" +
            "                {\n" +
            "                    name: \"百龙天梯\",\n" +
            "                    lat: 29.358901,\n" +
            "                    lng: 110.467890,\n" +
            "                    type: \"attraction\"\n" +
            "                }\n" +
            "            ];\n" +
            "            \n" +
            "            attractions.forEach(attraction => {\n" +
            "                const iconColor = attraction.type === 'main' ? 'red' : 'blue';\n" +
            "                const icon = L.divIcon({\n" +
            "                    html: `<div style=\"background-color: ${iconColor}; width: 20px; height: 20px; border-radius: 50%; border: 3px solid white; box-shadow: 0 2px 5px rgba(0,0,0,0.3);\"></div>`,\n" +
            "                    className: 'attraction-marker',\n" +
            "                    iconSize: [20, 20],\n" +
            "                    iconAnchor: [10, 10]\n" +
            "                });\n" +
            "                \n" +
            "                const marker = L.marker([attraction.lat, attraction.lng], {icon: icon}).addTo(map);\n" +
            "                marker.bindPopup(`<b>${attraction.name}</b>`);\n" +
            "            });\n" +
            "        }\n" +
            "        \n" +
            "        // 初始化预算图表\n" +
            "        function initBudgetChart() {\n" +
            "            const ctx = document.getElementById('budgetChart').getContext('2d');\n" +
            "            new Chart(ctx, {\n" +
            "                type: 'doughnut',\n" +
            "                data: {\n" +
            "                    labels: ['交通费用', '门票费用', '住宿费用', '餐饮费用'],\n" +
            "                    datasets: [{\n" +
            "                        data: [1200, 1368, 1200, 840],\n" +
            "                        backgroundColor: [\n" +
            "                            '#3b82f6',\n" +
            "                            '#10b981',\n" +
            "                            '#8b5cf6',\n" +
            "                            '#f59e0b'\n" +
            "                        ],\n" +
            "                        borderWidth: 2,\n" +
            "                        borderColor: '#ffffff'\n" +
            "                    }]\n" +
            "                },\n" +
            "                options: {\n" +
            "                    responsive: true,\n" +
            "                    plugins: {\n" +
            "                        legend: {\n" +
            "                            position: 'bottom'\n" +
            "                        },\n" +
            "                        tooltip: {\n" +
            "                            callbacks: {\n" +
            "                                label: function(context) {\n" +
            "                                    const label = context.label || '';\n" +
            "                                    const value = context.raw;\n" +
            "                                    const total = context.dataset.data.reduce((a, b) => a + b, 0);\n" +
            "                                    const percentage = Math.round((value / total) * 100);\n" +
            "                                    return `${label}: ¥${value} (${percentage}%)`;\n" +
            "                                }\n" +
            "                            }\n" +
            "                        }\n" +
            "                    }\n" +
            "                }\n" +
            "            });\n" +
            "        }\n" +
            "        \n" +
            "        // 地点导航功能\n" +
            "        function navigateTo(lat, lng, name) {\n" +
            "            if (/Android|webOS|iPhone|iPad|iPod|BlackBerry|IEMobile|Opera Mini/i.test(navigator.userAgent)) {\n" +
            "                // 移动端使用高德地图APP导航\n" +
            "                window.location.href = `amapuri://route/plan/?dlat=${lat}&dlon=${lng}&dname=${encodeURIComponent(name)}&dev=0&t=0`;\n" +
            "            } else {\n" +
            "                // PC端使用高德地图网页版\n" +
            "                window.open(`https://uri.amap.com/marker?position=${lng},${lat}&name=${encodeURIComponent(name)}`);\n" +
            "            }\n" +
            "        }\n" +
            "        \n" +
            "        // 页面加载完成后初始化\n" +
            "        document.addEventListener('DOMContentLoaded', function() {\n" +
            "            initMap();\n" +
            "            initBudgetChart();\n" +
            "            \n" +
            "            // 为所有地点链接添加点击事件\n" +
            "            document.querySelectorAll('.location-link').forEach(link => {\n" +
            "                link.addEventListener('click', function() {\n" +
            "                    const lat = this.getAttribute('data-lat');\n" +
            "                    const lng = this.getAttribute('data-lng');\n" +
            "                    const name = this.getAttribute('data-name');\n" +
            "                    navigateTo(lat, lng, name);\n" +
            "                });\n" +
            "            });\n" +
            "        });\n" +
            "    </script>\n" +
            "</body>\n" +
            "</html>\n";
}
