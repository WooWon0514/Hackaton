package com.example.demo.service;

import com.example.demo.dto.*;
import com.example.demo.dto.openai.OpenAIChatRequest;
import com.example.demo.dto.openai.OpenAIChatResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Service
public class ContractAnalysisService {

    @Value("${openai.api.key}")
    private String apiKey;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final String openAiUrl = "https://api.openai.com/v1/chat/completions";
    private final String apiModel = "gpt-4-turbo";

    private final boolean useMock = true;

    public AnalysisResponseDto diagnoseContract(ContractAnalysisRequestDto requestDto) {
        if (useMock) {
            System.out.println("--- MOCK API CALLED: diagnoseContract ---");

            // 이 부분을 아래 코드로 수정해주세요!
            List<AnalysisResultDto> mockResults = List.of(
                    new AnalysisResultDto(
                            2,
                            "업무 범위 및 대가",
                            "Yellow",
                            "기준 확인 필요",
                            null, // originalText
                            "대가 지급 시기 누락", // issueSummary
                            "대가 지급 시기가 구체적으로 명시되지 않았습니다." // reasoning
                    ),
                    new AnalysisResultDto(
                            3,
                            "저작권 조항",
                            "Red",
                            "협상 권장",
                            "제 5조. 모든 산출물의 저작재산권은 '갑'에게 귀속된다.", // originalText
                            "저작권 전면 양도", // issueSummary
                            "저작권이 전부 넘어가는 독소 조항입니다." // reasoning
                    ),
                    new AnalysisResultDto(
                            1,
                            "권리·의무 명확성",
                            "Blue",
                            "해석 여지 있음",
                            "제 3조. '을'은 합리적인 범위 내에서 업무를 수행한다.", // originalText
                            "업무 범위 모호", // issueSummary
                            "'합리적인 범위'는 해석의 여지가 있어 분쟁이 발생할 수 있습니다." // reasoning
                    )
            );
            return new AnalysisResponseDto(mockResults);
        }

        String systemPrompt = buildDiagnosisPrompt();
        String userPrompt = requestDto.getFullContractText();
        OpenAIChatRequest request = createChatRequest(systemPrompt, userPrompt, 2000);
        request.setResponseFormat(new OpenAIChatRequest.ResponseFormat("json_object"));
        try {
            String responseJson = postToOpenAI(request);
            return objectMapper.readValue(responseJson, AnalysisResponseDto.class);
        } catch (Exception e) {
            System.err.println("계약서 진단 중 오류 발생: " + e.getMessage());
            return new AnalysisResponseDto(List.of());
        }
    }

    // =================================================================================
    // STAGE 2: 조항 상세 설명 (Explanation) - (변경 없음)
    // =================================================================================
    public String getExplanation(ExplanationRequestDto requestDto) {
        if (useMock) {
            System.out.println("--- MOCK API CALLED: getExplanation ---");
            return "수정 횟수 제한이 없으면, 프로젝트가 끝난 뒤에도 '폰트 바꿔달라', '색 바꿔달라'는 연락이 계속 올 수 있어요.";
        }

        String systemPrompt = buildExplanationPrompt();
        String userPrompt = String.format("이슈: %s\n원문: %s", requestDto.getIssueSummary(), requestDto.getOriginalText() != null ? requestDto.getOriginalText() : "(누락됨)");
        OpenAIChatRequest request = createChatRequest(systemPrompt, userPrompt, 200);
        return postToOpenAI(request);
    }

    // =================================================================================
    // STAGE 3: 협상 메시지 작성 (Message Composition) - ✨ NEW & UPGRADED
    // =================================================================================
    public String composeMessage(MessageCompositionRequestDto requestDto) {
        if (useMock) {
            System.out.println("--- MOCK API CALLED: composeMessage (Upgraded) ---");
            // 제공해주신 3단 구조에 맞춘 Mock 응답
            return """
                    계약 진행 전에 업무 기준을 몇 가지 확인하고 싶습니다.

                    [기준 확인]
                    진행 기준을 명확히 하기 위해 아래 사항을 확인 부탁드립니다.
                    - 대금 지급 시기를 '프로젝트 완료 후 15일 이내'와 같이 구체적으로 명시할 수 있을까요?

                    [해석 정리]
                    계약 문구 해석에 대한 인식을 맞추고 싶습니다.
                    - '합리적인 범위'의 업무 요청 기준을 어떻게 생각하고 계신지 궁금합니다.

                    [조건 조율]
                    진행 안정성을 위해 조율 가능 여부를 확인하고 싶습니다.
                    - 저작권 귀속 범위를 '상업적 이용권'을 드리는 방향으로 조정 가능할까요?

                    위 기준을 정리한 후 해당 조건을 바탕으로 프로젝트를 진행하고자 합니다.
                    """;
        }

        String systemPrompt = buildMessageCompositionPrompt();
        try {
            // 사용자가 선택한 질문 리스트를 JSON 문자열로 변환하여 AI에게 전달
            String userPrompt = objectMapper.writeValueAsString(requestDto.getSelectedQuestions());
            OpenAIChatRequest request = createChatRequest(systemPrompt, userPrompt, 1500);
            return postToOpenAI(request);
        } catch (Exception e) {
            System.err.println("메시지 작성 중 JSON 변환 오류: " + e.getMessage());
            return "메시지를 생성하는 중 오류가 발생했습니다.";
        }
    }

    // =================================================================================
    // Private Helper & Prompt Methods
    // =================================================================================

    private OpenAIChatRequest createChatRequest(String systemPrompt, String userPrompt, int maxTokens) {
        OpenAIChatRequest.Message systemMessage = new OpenAIChatRequest.Message("system", systemPrompt);
        OpenAIChatRequest.Message userMessage = new OpenAIChatRequest.Message("user", userPrompt);
        return new OpenAIChatRequest(apiModel, List.of(systemMessage, userMessage), 0.5, maxTokens);
    }

    private String postToOpenAI(OpenAIChatRequest request) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);
        HttpEntity<OpenAIChatRequest> entity = new HttpEntity<>(request, headers);
        try {
            OpenAIChatResponse response = restTemplate.postForObject(openAiUrl, entity, OpenAIChatResponse.class);
            if (response != null && response.getChoices() != null && !response.getChoices().isEmpty()) {
                return response.getChoices().get(0).getMessage().getContent();
            }
            return "AI로부터 응답을 받지 못했습니다.";
        } catch (Exception e) {
            System.err.println("OpenAI API 호출 중 오류 발생: " + e.getMessage());
            return "AI 서버와 통신 중 오류가 발생했습니다.";
        }
    }

    private String buildDiagnosisPrompt() {
        // (변경 없음)
        return """
                # Role
                당신은 프리랜서와 크리에이터의 권익을 보호하는 20년 경력의 계약 전문 변호사입니다.
                # Task
                제공된 계약서 텍스트를 [Analysis Guidelines]에 따라 분석하고, 각 이슈를 [Badge Mapping Rules]에 대입하여 위험도를 평가한 뒤, 결과를 JSON 포맷으로 출력하세요.
                ---
                # 1. Analysis Guidelines (탐지 기준)
                1. 권리·의무 명확성: '합리적', '관례' 등 모호한 표현이 없는지 확인.
                2. 업무 범위 및 대가: 금액, 지급 시기, 수정 횟수, 추가금 조항이 구체적으로 명시되었는지 확인.
                3. 저작권 조항: 저작재산권 양도 여부, 2차 저작물 작성권, 저작인격권 포기 강요 여부 확인.
                4. 저작물-계약 매칭: '결과물'의 범위가 실제 과업과 일치하는지 확인.
                5. 권리 귀속 구조: 포트폴리오 사용권 허용 여부, 라이선스 범위 확인.
                6. 계약 기간 및 종료: 계약 해지 사유의 공정성, 종료 후 비밀유지 기간 확인.
                ---
                # 2. Badge Mapping Rules (위험도 판정 매트릭스)
                Priority 1. 🟨 Yellow: Critical Missing
                - [Rule 1.1] '2. 업무 범위/대가'에서 금액, 지급 시기, 기본 수정 횟수 중 하나라도 누락.
                - [Rule 1.2] '4. 저작물 매칭'에서 계약의 목적물(과업 대상)이 명시되지 않은 경우.
                Priority 2. 🟥 Red: Toxic / Unfair
                - [Rule 2.1] '3. 저작권 조항'에서 저작재산권 일체를 '전면 양도'하거나, 저작인격권을 포기하게 만드는 경우.
                - [Rule 2.2] '2. 업무 범위/대가'에서 '무제한 수정'을 요구하거나, 추가 수정 비용을 인정하지 않는 경우.
                Priority 3. 🟦 Blue: Ambiguous
                - [Rule 3.1] '합리적인', '상당한', '협의하여' 등 주관적 해석이 가능한 단어가 포함된 경우.
                Priority 4. 🟩 Green: Optional Missing
                - [Rule 4.1] '5. 권리 귀속'에서 '포트폴리오 사용권'에 대한 언급이 아예 없는 경우.
                ---
                # 3. Output Format (JSON Only)
                { "analysis_results": [ { "criteria_id": 1, "category_name": "권리·의무 명확성", "badge_color": "Blue", "badge_label": "해석 여지 있음", "original_text": "제 3조. '을'은 합리적인 범위 내에서 '갑'의 요청에 따라 업무를 수행한다.", "issue_summary": "의무 범위의 모호성", "reasoning": "'합리적인 범위'라는 표현은 주관적 해석의 여지가 있어 분쟁의 원인이 될 수 있습니다. (Rule 3.1 적용)" } ] }
                """;
    }

    private String buildExplanationPrompt() {
        // (변경 없음)
        return """
                # Role
                당신은 프리랜서 디자이너의 현실적인 고충을 깊이 이해하는 동료 멘토입니다. 계약서의 독소조항이 실무에서 어떤 피해로 이어지는지 '피부로 와닿게' 경고해주세요.
                # Input Format
                - 이슈: {issue_summary}
                - 원문: {original_text}
                # Task
                이 조항을 그대로 두면 발생할 구체적인 시나리오를 1~2문장의 짧은 글로 작성하세요. 법률 용어를 절대 사용하지 마세요.
                # Writing Rules (Tone & Manner)
                1. '돈'과 '시간'으로 환산하세요: "불리합니다" 대신 "추가금을 못 받습니다" 또는 "주말에도 일해야 합니다"라고 하세요.
                2. 사용자를 직접 부르세요: "프리랜서는~" 이라고 하지 말고 "디자이너님은~" 또는 바로 상황을 묘사하세요.
                # Few-Shot Examples (이 말투와 스타일을 그대로 모방하세요)
                ---
                [EXAMPLE 1]
                Input:
                - 이슈: 수정 횟수 미기재
                - 원문: 을은 갑의 요청에 따라 결과물을 수정한다.
                Output:
                수정 횟수 제한이 없으면, 프로젝트가 끝난 뒤에도 '폰트 바꿔달라', '색 바꿔달라'는 연락이 계속 올 수 있어요.
                ---
                # Final Instruction
                서론, 결론, 부연 설명 없이 'Output'에 해당하는 경고 문구만 응답하세요. Plain Text로만 출력하세요.
                """;
    }

    // ✨ NEW & UPGRADED Message Composition Prompt
    private String buildMessageCompositionPrompt() {
        // 제공해주신 '예시 2'를 기반으로 프롬프트를 재구성했습니다.
        return """
                # Role
                당신은 정중하고 유능한 비즈니스 커뮤니케이션 매니저입니다. 감정을 배제하고, '프로젝트의 성공적인 완수'를 명분으로 협상을 제안합니다.

                # Input
                사용자가 선택한 질문 리스트가 JSON 배열 형식으로 제공됩니다.
                (예: [{"badge": "Yellow", "question": "수정은 몇 회까지를 기본으로 보시는지"}])

                # Task
                입력된 질문들을 논리적 흐름(3단 구조)에 맞춰 재배치하고, 클라이언트에게 보낼 완성된 이메일 본문을 작성하세요.

                # Structure Rules (섹션 자동 분류)
                입력된 질문의 'badge' 값을 기준으로 문단을 나누세요. 해당 색상의 질문이 없으면 그 섹션은 출력하지 마세요.

                1. [기준 확인] (badge가 "Yellow" 또는 "Green"인 경우)
                   - 도입 문구: "진행 기준을 명확히 하기 위해 아래 사항을 확인 부탁드립니다."
                   - 내용: 관련된 질문들을 글머리 기호(-)를 사용하여 나열.

                2. [해석 정리] (badge가 "Blue"인 경우)
                   - 도입 문구: "계약 문구 해석에 대한 인식을 맞추고 싶습니다."
                   - 내용: 관련된 질문들을 글머리 기호(-)를 사용하여 나열.

                3. [조건 조율] (badge가 "Red"인 경우)
                   - 도입 문구: "진행 안정성을 위해 조율 가능 여부를 확인하고 싶습니다."
                   - 내용: 관련된 질문들을 글머리 기호(-)를 사용하여 나열.

                # Constraints
                - 서두 인사: "계약 진행 전에 업무 기준을 몇 가지 확인하고 싶습니다." 라는 문장으로 반드시 시작하세요.
                - 맺음말: "위 기준을 정리한 후 해당 조건을 바탕으로 프로젝트를 진행하고자 합니다." 라는 문장으로 반드시 끝내세요.
                - 문체: 정중하되, "문의드립니다" 보다는 "확인이 필요합니다" 또는 "조율하고 싶습니다" 와 같은 능동적인 표현을 사용하세요.
                - 출력: 다른 설명 없이, 완성된 이메일 본문 텍스트만 출력하세요.
                """;
    }
}