package indi.dkx.laocai.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChatCompletionResponse {
    private String id;
    private List<Choice> choices;
    private Usage usage;
    private Integer created;
    private String model;
    private String object;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Choice {
        private Message message;
        private String finish_reason;

        @Data
        @AllArgsConstructor
        @NoArgsConstructor
        public static class Message {
            private String role;
            private String content;
            private String reasoning_content;
            private List<ToolCall> tool_calls;

            @Data
            @AllArgsConstructor
            @NoArgsConstructor
            public static class ToolCall {
                private String id;
                private String type;
                private Function function;

                @Data
                @AllArgsConstructor
                @NoArgsConstructor
                public static class Function {
                    private String name;
                    private String arguments;
                }
            }
        }
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    private static class Usage {
        private Integer prompt_tokens;
        private Integer completion_tokens;
        private Integer total_tokens;
    }
}
