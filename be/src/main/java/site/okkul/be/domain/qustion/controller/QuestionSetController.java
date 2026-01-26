package site.okkul.be.domain.qustion.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;
import site.okkul.be.domain.qustion.service.QuestionSetService;

/**
 * @author 김남주
 */
@RestController
@RequiredArgsConstructor
public class QuestionSetController implements QuestionSetControllerDocs {
	private final QuestionSetService questionSetService;
}
