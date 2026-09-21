package com.hope.chufala.model.vo;

public class TouristAttraction {
        private Long id;
        private String name;
        private String location;
        private Double rating;
        private String description;

        public TouristAttraction() {}

        public TouristAttraction(Long id, String name, String location, Double rating, String description) {
            this.id = id;
            this.name = name;
            this.location = location;
            this.rating = rating;
            this.description = description;
        }

        // Getters and Setters
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public String getLocation() { return location; }
        public void setLocation(String location) { this.location = location; }

        public Double getRating() { return rating; }
        public void setRating(Double rating) { this.rating = rating; }

        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }

        @Override
        public String toString() {
            return String.format("Attraction{id=%d, name='%s', location='%s', rating=%.1f}",
                    id, name, location, rating);
        }
    }

