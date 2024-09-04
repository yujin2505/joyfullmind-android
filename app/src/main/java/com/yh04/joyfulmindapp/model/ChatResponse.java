package com.yh04.joyfulmindapp.model;

import com.google.gson.annotations.SerializedName;

public class ChatResponse {
    @SerializedName("result")
    private String result;
    @SerializedName("answer")
    private String answer;
    @SerializedName("timeStamp")
    private String timeStamp;

    public ChatResponse() {
    }

    public ChatResponse(String result, String answer, String timeStamp) {
        this.result = result;
        this.answer = answer;
        this.timeStamp = timeStamp;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }

    public String getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(String timeStamp) {
        this.timeStamp = timeStamp;
    }

    @Override
    public String toString() {
        return "ChatResponse{" +
                "result='" + result + '\'' +
                ", answer='" + answer + '\'' +
                ", timeStamp='" + timeStamp + '\'' +
                '}';
    }
}
