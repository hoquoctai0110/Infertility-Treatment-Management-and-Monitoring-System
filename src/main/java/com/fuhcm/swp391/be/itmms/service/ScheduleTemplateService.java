package com.fuhcm.swp391.be.itmms.service;

import com.fuhcm.swp391.be.itmms.dto.request.ScheduleTemplateRequest;
import com.fuhcm.swp391.be.itmms.dto.response.ScheduleTemplateResponse;
import com.fuhcm.swp391.be.itmms.entity.Account;
import com.fuhcm.swp391.be.itmms.entity.ScheduleTemplate;
import com.fuhcm.swp391.be.itmms.entity.Shift;
import com.fuhcm.swp391.be.itmms.repository.AccountRepository;
import com.fuhcm.swp391.be.itmms.repository.ScheduleTemplateRepository;
import com.fuhcm.swp391.be.itmms.repository.ShiftRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ScheduleTemplateService {
    private final ScheduleTemplateRepository scheduleTemplateRepository;
    private final ShiftRepository shiftRepository;


    @Transactional
    public ScheduleTemplateResponse createTemplateStaff(ScheduleTemplateRequest request) {
        Shift shift = shiftRepository.findById(request.getShiftId()).get();
        boolean exists = scheduleTemplateRepository.existsByDayOfWeekAndShift(request.getDayOfWeek(), shift);
        if(exists) throw new RuntimeException("Schedule template already exists");

        ScheduleTemplate newTemplate = new ScheduleTemplate();
        newTemplate.setDayOfWeek(request.getDayOfWeek());
        newTemplate.setMaxStaffs(request.getMaxStaffs());
        newTemplate.setMaxDoctors(request.getMaxDoctors());
        newTemplate.setShift(shift);
        scheduleTemplateRepository.save(newTemplate);

        return new ScheduleTemplateResponse(newTemplate);
    }

    public ScheduleTemplateResponse updateScheduleTemplate(@Valid ScheduleTemplateRequest request,
                                                           @Valid @Min(1) Long id) {
        ScheduleTemplate  scheduleTemplate = scheduleTemplateRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Schedule template not found"));
        scheduleTemplate.setDayOfWeek(request.getDayOfWeek());
        scheduleTemplate.setMaxStaffs(request.getMaxStaffs());
        scheduleTemplate.setMaxDoctors(request.getMaxDoctors());
        Shift shift = shiftRepository.findById(request.getShiftId())
                .orElseThrow(() -> new RuntimeException("Shift not found"));
        scheduleTemplate.setShift(shift);
        scheduleTemplateRepository.save(scheduleTemplate);
        return new ScheduleTemplateResponse(scheduleTemplate);
    }

    public List<ScheduleTemplateResponse> getAllScheduleTemplate() {
        List<ScheduleTemplateResponse> responses = new ArrayList<>();
        List<ScheduleTemplate>  scheduleTemplates = scheduleTemplateRepository.findAll();
        for(ScheduleTemplate scheduleTemplate : scheduleTemplates){
            responses.add(new ScheduleTemplateResponse(scheduleTemplate));
        }
        return responses;
    }
}
