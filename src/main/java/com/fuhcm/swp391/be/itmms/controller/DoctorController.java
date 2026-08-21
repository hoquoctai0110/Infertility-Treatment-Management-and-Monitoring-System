package com.fuhcm.swp391.be.itmms.controller;


import com.fuhcm.swp391.be.itmms.dto.request.DoctorRequest;
import com.fuhcm.swp391.be.itmms.dto.response.DoctorResponse;
import com.fuhcm.swp391.be.itmms.dto.response.ResponseFormat;
import com.fuhcm.swp391.be.itmms.service.DoctorService;
import javassist.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@RequestMapping("/api/doctors")
public class DoctorController {
    private final DoctorService doctorService;

    @GetMapping("/home")
    public ResponseEntity getDoctorsInHomePage() {
        return ResponseEntity.ok(
                new ResponseFormat<>(HttpStatus.OK.value(),
                                            "FETCH_SUCCESS",
                                        "Lấy dữ liệu thành công",
                                                doctorService.getDoctorsInHomePage())
        );
    }

    @GetMapping("/accounts")
    public ResponseEntity getDoctorAccount() {
        return ResponseEntity.ok(
                new ResponseFormat<>(HttpStatus.OK.value(),
                        "FETCH_SUCCESS",
                        "Lấy dữ liệu thành công",
                        doctorService.getDoctorAccount())
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity getDoctorById(@PathVariable Long id) throws NotFoundException {
        DoctorResponse response = doctorService.getDoctorById(id);
        return ResponseEntity.ok(new ResponseFormat<>(HttpStatus.OK.value(),
                "FETCH_SUCCESS",
                "Lấy thông tin bác sĩ thành công",
                response));
    }

    @GetMapping("/details")
    public ResponseEntity<?> getDoctorByEmail(@RequestParam String email) throws NotFoundException {
        DoctorResponse doctor = doctorService.getDoctorByEmail(email);
        return ResponseEntity.ok(
                new ResponseFormat<>(HttpStatus.OK.value(),
                        "FETCH_SUCCESS",
                        "Lấy thông tin bác sĩ thành công",
                        doctor)
        );
    }

    @PutMapping("/details/{id}")
    public ResponseEntity<?> updateDoctor(@PathVariable("id") Long doctorId,
                                          @RequestBody DoctorRequest request) throws NotFoundException {
        DoctorResponse response = doctorService.updateDoctor(doctorId, request);
        return ResponseEntity.ok(
                new ResponseFormat<>(HttpStatus.OK.value(),
                        "UPDATE_SUCCESS",
                        "Cập nhật thông tin bác sĩ thành công",
                        response)
        );
    }

    @PostMapping("/details")
    public ResponseEntity<?> createDoctor(@RequestBody DoctorRequest request) throws NotFoundException {
        DoctorResponse response = doctorService.createDoctor(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(
                new ResponseFormat<>(HttpStatus.CREATED.value(),
                        "CREATE_SUCCESS",
                        "Tạo bác sĩ thành công",
                        response)
        );
    }
}
