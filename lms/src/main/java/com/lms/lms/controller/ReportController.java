package com.lms.lms.controller;

import com.lms.lms.service.ReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/report")
public class ReportController {

    private final ReportService reportService;

    @Autowired
    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    // مسار تحميل تقرير الإكسيل للطلاب في كورس معين
    @GetMapping("/{courseId}/excel")
    public ResponseEntity<byte[]> downloadExcelReport(@PathVariable int courseId) {
        try {
            byte[] reportBytes = reportService.generateExcelReport(courseId);

            HttpHeaders headers = new HttpHeaders();
            // تحديد نوع الملف كـ Excel
            headers.setContentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
            // إجبار المتصفح على تحميل الملف وتسميته
            headers.setContentDispositionFormData("attachment", "Course_" + courseId + "_Report.xlsx");

            return new ResponseEntity<>(reportBytes, headers, HttpStatus.OK);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // مسار تحميل الرسم البياني (Chart) لأداء الطلاب
    @GetMapping("/{courseId}/chart")
    public ResponseEntity<byte[]> downloadPerformanceChart(@PathVariable int courseId) {
        try {
            byte[] chartBytes = reportService.generatePerformanceChart(courseId);

            HttpHeaders headers = new HttpHeaders();
            // تحديد نوع الملف كصورة PNG
            headers.setContentType(MediaType.IMAGE_PNG);
            // إجبار المتصفح على تحميل الصورة
            headers.setContentDispositionFormData("attachment", "Course_" + courseId + "_Performance_Chart.png");

            return new ResponseEntity<>(chartBytes, headers, HttpStatus.OK);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}