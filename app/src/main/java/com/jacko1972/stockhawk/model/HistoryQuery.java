package com.jacko1972.stockhawk.model;


import com.google.gson.annotations.SerializedName;

public class HistoryQuery {

        @SerializedName("count")
        private Integer count;
        @SerializedName("created")
        private String created;
        @SerializedName("lang")
        private String lang;
        @SerializedName("results")
        private HistoryResults results;

        public Integer getCount() {
            return count;
        }

        public void setCount(Integer count) {
            this.count = count;
        }

        public String getCreated() {
            return created;
        }

        public void setCreated(String created) {
            this.created = created;
        }

        public String getLang() {
            return lang;
        }

        public void setLang(String lang) {
            this.lang = lang;
        }

        public HistoryResults getResults() {
            return results;
        }

        public void setResults(HistoryResults results) {
            this.results = results;
        }

    }
