package com.example.demo.controller;

import com.example.demo.dto.ContractAnalysisRequestDto;
import com.example.demo.dto.ExplanationRequestDto;
import com.example.demo.dto.MessageCompositionRequestDto;
import com.example.demo.dto.AnalysisResponseDto;
import com.example.demo.service.ContractAnalysisService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/contracts")
public class ContractAnalysisController {

    private final ContractAnalysisService contractAnalysisService;

    // 생성자를 통한 의존성 주입
    public ContractAnalysisController(ContractAnalysisService contractAnalysisService) {
        this.contractAnalysisService = contractAnalysisService;
    }

    /**
     * STAGE 1: 계약서 진단 API
     * @param requestDto 계약서 전체 텍스트를 담은 DTO
     * @return 분석 결과를 담은 DTO
     */
    @PostMapping("/diagnose") // URL도 diagnose로 통일하는 것을 추천합니다.
    public AnalysisResponseDto diagnoseContract(@RequestBody ContractAnalysisRequestDto requestDto) {
        // 여기서 메소드 이름을 analyzeContract -> diagnoseContract 로 변경했습니다.
        return contractAnalysisService.diagnoseContract(requestDto);
    }

    /**
     * STAGE 2: 개별 조항 설명 API
     * @param requestDto 설명이 필요한 이슈 정보를 담은 DTO
     * @return AI가 생성한 쉬운 설명 텍스트
     */
    @PostMapping("/explain")
    public String getExplanation(@RequestBody ExplanationRequestDto requestDto) {
        return contractAnalysisService.getExplanation(requestDto);
    }

    /**
     * STAGE 3: 협상 메시지 작성 API
     * @param requestDto 사용자가 선택한 질문 목록을 담은 DTO
     * @return AI가 생성한 완성된 이메일 초안
     */
    @PostMapping("/compose-message")
    public String composeMessage(@RequestBody MessageCompositionRequestDto requestDto) {
        return contractAnalysisService.composeMessage(requestDto);
    }
}