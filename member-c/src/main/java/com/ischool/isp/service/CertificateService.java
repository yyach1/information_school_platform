package com.ischool.isp.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.ischool.isp.dto.request.*;
import com.ischool.isp.dto.response.*;

import java.util.List;

public interface CertificateService {

    List<CertificateTypeResponse> getCertificateTypes();

    CertificateResponse apply(CertificateApplyRequest request);

    IPage<CertificateListItemResponse> listMyCertificates(Integer page, Integer pageSize, String status, String certType);

    CertificateResponse getDetail(Long id);

    CertificateResponse resubmit(Long id, CertificateResubmitRequest request);

    CertificateResponse downloadPdfUrl(Long id);

    IPage<CertificateResponse> adminList(Integer page, Integer pageSize, String status, String certType, String grade, String className);

    void audit(Long id, CertificateAuditRequest request);

    void issue(Long id, CertificateIssueRequest request);

    java.util.Map<String, Long> getStats();
}
