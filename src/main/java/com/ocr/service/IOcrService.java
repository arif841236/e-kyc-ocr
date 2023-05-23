package com.ocr.service;

import java.io.IOException;
import org.springframework.web.multipart.MultipartFile;
import com.ocr.model.BankStatementRequest;
import com.ocr.model.FaceDedupeRequest;
import com.ocr.model.OcrRequest;
import javax.servlet.http.HttpServletResponse;

public interface IOcrService {

	public String getKycDetails(OcrRequest adharRequest, MultipartFile file)throws IOException;
	public String getBankStatement(BankStatementRequest bankStatementRequest,HttpServletResponse httpServletResponse) throws IOException;
	public String getFaceDedupe(FaceDedupeRequest faceDedupeRequest) throws IOException;
}
