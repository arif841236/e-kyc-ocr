package com.ocr.util;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.ocr.exception.OcrException;
import com.ocr.model.OcrRequest;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class OcrResponse {

	@Autowired
	Gson gson;

	@Autowired
	LoggingMessageUtil logger;

	@Autowired
	Environment environment;

	@Value("${uri.connection.error}")
	String uriError;

	@Value("${file.size.error}")
	String fileSizeError;

	@Value("${content.key}")
	String contentKey;

	@Value("${content.value}")
	String contentValue;

	@Value("${content.file.key}")
	String contentFileKey;

	String requestId2 = "requestId";
	String sStatusCode = "statusCode";

	public String getPanOcr(File kycImage) throws IOException {
		String url = environment.getProperty("endpoint.uri.ocr.pan");

		checkFileLength(kycImage.length());

		HttpEntity<MultiValueMap<String, Object>> httpEntity = getHttpHeader(kycImage,contentFileKey);

		JsonObject asJsonObject = getJsonObjectWithRestemplate(httpEntity,url,HttpMethod.POST);
		Files.delete(kycImage.toPath());

		return gson.toJson(asJsonObject);
	}

	public String getAadhaarData(OcrRequest kycRequest, File kycImage) throws IOException {
		String uri = "";
		String val = "value";
		String adharFront = "aadhaar-front";
		String resultString = "result";

		if(kycRequest.getDocType().equalsIgnoreCase(adharFront)) {
			uri = environment.getProperty("endpoint.uri.ocr.aadhaar.front");
		}
		else if(kycRequest.getDocType().equalsIgnoreCase("aadhaar-back")) {
			uri = environment.getProperty("endpoint.uri.ocr.aadhaar.back");
		}

		HttpEntity<MultiValueMap<String, Object>> httpEntity = getHttpHeader(kycImage,contentFileKey);

		JsonObject responseObject = getJsonObjectWithRestemplate(httpEntity,uri,HttpMethod.POST);
		Files.delete(kycImage.toPath());

		if(responseObject.get(resultString) == null || responseObject.get(resultString).getAsJsonArray().get(0).getAsJsonObject().get("details") == null) {
			throw new OcrException("Aadhaar details are not found.");
		}

		JsonObject details = responseObject.get(resultString).getAsJsonArray().get(0).getAsJsonObject().get("details").getAsJsonObject();
		if(kycRequest.getDocType().equalsIgnoreCase("aadhaar-back")) {
			details.remove("name");
			details.remove("gender");
			details.remove("dob");
			details.remove("yob");
			details.remove("phone");
		}

		details.get("imageUrl").getAsJsonObject().addProperty("value", "");
		JsonObject aadhaarJson1 = details.get("aadhaar").getAsJsonObject();
		String aadhaarNumber = aadhaarJson1.get(val).getAsString();
		if(kycRequest.getDocType().equalsIgnoreCase(adharFront) && kycRequest.isHideAadhaar()) {
			aadhaarJson1.addProperty(val, "");
		}
		else if(kycRequest.getDocType().equalsIgnoreCase(adharFront) && kycRequest.isMaskAadhaar()) {
			aadhaarNumber = String.join("", aadhaarNumber.split(" "));
			int length2 = aadhaarNumber.length();
			String replace = aadhaarNumber.substring(0, length2-4);
			aadhaarNumber = aadhaarNumber.replaceAll(replace, "xxxxxxxx");
			aadhaarJson1.addProperty(val, aadhaarNumber);
			aadhaarJson1.addProperty("isMasked", "yes");
		}

		return gson.toJson(responseObject);
	}

	public String getDlOcr(File kycImage, OcrRequest ocrRequest) throws IOException {
		String uri = null;
		String fileContent = "";
		if(ocrRequest.getDocType().equalsIgnoreCase("dl-front")) {
			uri = environment.getProperty("endpoint.uri.ocr.dl");
			fileContent = "front_dl";
		}
		else if(ocrRequest.getDocType().equalsIgnoreCase("dl-back")) {
			uri = environment.getProperty("endpoint.uri.ocr.dl.back");
			fileContent = "back_dl";
		}
		else {
			throw new OcrException("Doct type should be valid.");
		}

		HttpEntity<MultiValueMap<String, Object>> httpEntity = getHttpHeader(kycImage,fileContent);

		JsonObject asJsonObject = getJsonObjectWithRestemplate(httpEntity,uri,HttpMethod.POST);
		Files.delete(kycImage.toPath());
		asJsonObject.addProperty(sStatusCode, 101);

		return gson.toJson(asJsonObject);
	}

	public String getPassPostData(OcrRequest kycRequest, File kycImage) throws IOException {
		String uri = "";
		String passPortFront = "passport-front";
		if(kycRequest.getDocType().equalsIgnoreCase(passPortFront)) {
			uri = environment.getProperty("endpoint.uri.ocr.passport.front");
		}
		else if(kycRequest.getDocType().equalsIgnoreCase("passport-back")) {
			uri = environment.getProperty("endpoint.uri.ocr.passport.back");
		}

		HttpEntity<MultiValueMap<String, Object>> httpEntity = getHttpHeader(kycImage,contentFileKey);

		JsonObject responseObject = getJsonObjectWithRestemplate(httpEntity,uri,HttpMethod.POST);
		Files.delete(kycImage.toPath());
		responseObject.addProperty(sStatusCode, 101);
		return gson.toJson(responseObject);
	}

	public String getPaySlipOcr(File kycImage) throws IOException {
		String uri = environment.getProperty("endpoint.uri.ocr.payslip");

		HttpEntity<MultiValueMap<String, Object>> httpEntity = getHttpHeader(kycImage,contentFileKey);

		JsonObject jsonObject = getJsonObjectWithRestemplate(httpEntity,uri,HttpMethod.POST);
		Files.delete(kycImage.toPath());
		jsonObject.addProperty(sStatusCode, 101);

		return gson.toJson(jsonObject);
	}

	public byte[] getBankStatementDataInFile(File kycImage) throws IOException {

		String uri = environment.getProperty("endpoint.uri.ocr.bankstatement");

		HttpEntity<MultiValueMap<String, Object>> httpEntity = getHttpHeader(kycImage,contentFileKey);

		byte[] byteResponse = null;
		if(uri != null && !uri.isEmpty()) {
			log.info(uri);
			ResponseEntity<byte[]> response = new RestTemplate().exchange(uri, HttpMethod.POST, httpEntity,
					byte[].class);
			log.info(uri);
			byteResponse = response.getBody();
		}
		else {
			throw new OcrException(uriError);
		}
		log.info(uri);
		Files.delete(kycImage.toPath());

		return byteResponse;
	}

	public String getBankStatementDataInJson(File kycImage) throws IOException {
		String uri = environment.getProperty("endpoint.uri.ocr.bankstatement.json");

		HttpEntity<MultiValueMap<String, Object>> httpEntity = getHttpHeader(kycImage,contentFileKey);

		JsonObject jsonObject = getJsonObjectWithRestemplate(httpEntity,uri,HttpMethod.POST);
		Files.delete(kycImage.toPath());
		jsonObject.addProperty(sStatusCode, 101);

		return gson.toJson(jsonObject);
	}

	public String getFaceDedupeResult(File file1, File file2) throws IOException {
		String uri = environment.getProperty("endpoint.uri.ocr.face.dedupe");

		long length = file1.length();
		long length2 = file2.length();
		if(length > 5242880 || length2 > 5242880) {
			throw new OcrException(fileSizeError);
		}

		Resource resource = new FileSystemResource(file1);
		Resource resource2 = new FileSystemResource(file2);

		MultiValueMap<String, Object> parts = new LinkedMultiValueMap<>();
		MultiValueMap<String, Object> contentParts = new LinkedMultiValueMap<>();
		contentParts.add(contentKey, contentValue);
		contentParts.add(contentKey, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
		parts.addAll(contentParts);
		parts.add("img1", resource);
		parts.add("img2", resource2);
		log.info(parts.toString());

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.MULTIPART_FORM_DATA);

		HttpEntity<MultiValueMap<String, Object>> httpEntity = new HttpEntity<>(parts,headers);
		JsonObject jsonObject = getJsonObjectWithRestemplate(httpEntity, uri, HttpMethod.POST);

		Files.delete(file1.toPath());
		Files.delete(file2.toPath());

		return gson.toJson(jsonObject);
	}

	private HttpEntity<MultiValueMap<String, Object>> getHttpHeader(File kycImage,String contentFile) {
		Resource resource = new FileSystemResource(kycImage);
		checkFileLength(kycImage.length());
		MultiValueMap<String, Object> parts = new LinkedMultiValueMap<>();
		MultiValueMap<String, Object> contentParts = new LinkedMultiValueMap<>();
		contentParts.add(contentKey, contentValue);
		contentParts.add(contentKey, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
		contentParts.add(contentKey, "application/pdf");
		parts.addAll(contentParts);
		parts.add(contentFile, resource);
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.MULTIPART_FORM_DATA);

		return new HttpEntity<>(parts,headers);
	}

	private JsonObject getJsonObjectWithRestemplate(HttpEntity<MultiValueMap<String, Object>> httpEntity, String url,
			HttpMethod post) {

		Object result = null;
		if(url != null && !url.isEmpty()) {
			ResponseEntity<Object> resultResponse = new RestTemplate().exchange(url,post,httpEntity,Object.class);
			result = resultResponse.getBody();
		}
		else {
			throw new OcrException(uriError);
		}

		String pan = gson.toJson(result);
		String requestId = UUID.randomUUID().toString().substring(0, 36);
		JsonObject asJsonObject = pan.substring(0, 1).equals("{")?gson.fromJson(pan, JsonObject.class)
				:gson.fromJson(pan, JsonArray.class).get(0).getAsJsonObject();
		asJsonObject.addProperty(requestId2, requestId);

		return asJsonObject;
	}

	private void checkFileLength(long length) {
		if(length > 5242880) {
			throw new OcrException(fileSizeError);
		}
	}
}