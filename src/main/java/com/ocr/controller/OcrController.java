package com.ocr.controller;

import java.io.IOException;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import com.google.gson.Gson;
import com.ocr.exception.ExceptionHandlerGlobal;
import com.ocr.model.BankStatementRequest;
import com.ocr.model.FaceDedupeRequest;
import com.ocr.model.OcrRequest;
import com.ocr.service.IOcrService;
import com.ocr.util.LoggingMessageUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;

/**
 * This is controller layer
 * @author Md Arif
 *
 */
@RestController
@Tag(description = "OCR API", name = "OCR service")
@Slf4j
public class OcrController {

	@Autowired
	private IOcrService kycService;

	@Autowired
	LoggingMessageUtil loggingMessageUtil;

	@Autowired
	Gson gson;

	@ApiResponse(description = "Get OCR data successfully.",responseCode = "200", content = {@Content(mediaType = MediaType.APPLICATION_JSON_VALUE,schema = @Schema(implementation = Object.class))})
	@Operation(description = "Fetch document image data",summary = "Fetch document data",method = "POST")
	@io.swagger.v3.oas.annotations.parameters.RequestBody (description = "Fill the ocr request body", required = true,content = @Content(schema = @Schema(implementation = OcrRequest.class))) 
	@PostMapping(value="/ocr",consumes = {MediaType.APPLICATION_JSON_VALUE}, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<String> getKycData(@RequestBody @Valid OcrRequest kycRequest) throws IOException{
		log.info(gson.toJson(kycRequest.getDocType() +" Request Initiated "+ kycRequest.getSource()));
		ExceptionHandlerGlobal.initSource(kycRequest.getSource());

		String aadhaardDetails = kycService.getKycDetails(kycRequest,null);

		log.info(gson.toJson("Response Sent to Source Successfully for " + kycRequest.getDocType()));

		return ResponseEntity.ok(aadhaardDetails);
	}

	@ApiResponse(description = "Get Bank statement successfully.",responseCode = "200", content = {@Content(mediaType = MediaType.APPLICATION_JSON_VALUE,schema = @Schema(implementation = Object.class))})
	@Operation(description = "Get bank statement data",summary = "Get statement",method = "POST")
	@io.swagger.v3.oas.annotations.parameters.RequestBody (description = "Fill the bank statement request body", required = true,content = @Content(schema = @Schema(implementation = BankStatementRequest.class))) 
	@PostMapping(value="/statement", consumes = {MediaType.APPLICATION_JSON_VALUE}, produces = {MediaType.MULTIPART_FORM_DATA_VALUE,MediaType.APPLICATION_JSON_VALUE})
	public ResponseEntity<String> getFileDetail(@RequestBody @Valid BankStatementRequest statementRequest,HttpServletResponse httpServletResponse) throws IOException{
		log.info(gson.toJson("Bank statement Request Initiated "+ statementRequest.getSource()));
		ExceptionHandlerGlobal.initSource(statementRequest.getSource());
		String bankStatement = kycService.getBankStatement(statementRequest, httpServletResponse);
		log.info(gson.toJson("Response Sent to Source Successfully for bank statement."));

		return ResponseEntity.ok(bankStatement);
	}
	
	@ApiResponse(description = "Get face dedupe data successfully.",responseCode = "200", content = {@Content(mediaType = MediaType.APPLICATION_JSON_VALUE,schema = @Schema(implementation = Object.class))})
	@Operation(description = "Get face dedupe data",summary = "Get face dedupe",method = "POST")
	@io.swagger.v3.oas.annotations.parameters.RequestBody (description = "Fill the face dedupe request body", required = true,content = @Content(schema = @Schema(implementation = FaceDedupeRequest.class))) 
	@PostMapping(value="/faceDedupe",consumes = {MediaType.APPLICATION_JSON_VALUE}, produces = {MediaType.APPLICATION_JSON_VALUE})
	public ResponseEntity<String> faceDedupeDetail(@RequestBody @Valid FaceDedupeRequest dedupeRequest) throws IOException{
		log.info(gson.toJson("Face dedupe Request Initiated "+ dedupeRequest.getSource()));
		ExceptionHandlerGlobal.initSource(dedupeRequest.getSource());
		String bankStatement = kycService.getFaceDedupe(dedupeRequest);
		log.info(gson.toJson("Response Sent to Source Successfully for face dedupe."));

		return ResponseEntity.ok(bankStatement);
	}
}