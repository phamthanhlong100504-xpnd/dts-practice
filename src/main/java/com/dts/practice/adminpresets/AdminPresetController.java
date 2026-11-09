package com.dts.practice.adminpresets;

import com.dts.practice.adminpresets.AdminPresetModels.*;
import com.dts.practice.dto.response.ApiResponse;
import com.dts.practice.security.JwtUserDetails;
import java.util.UUID;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/** Admin-only endpoints. No Publish/Archive or mutation of any existing learner preset. */
@RestController
@RequestMapping("/api/v1/exams/admin")
public class AdminPresetController {
    private final AdminPresetService service;
    public AdminPresetController(AdminPresetService service){this.service=service;}

    @GetMapping("/presets")
    @PreAuthorize("hasRole('ADMIN') and hasAuthority('PERM_practice-exam-sets:read')")
    public ResponseEntity<ApiResponse<Page>> list(
            @RequestParam(required=false) String licenseClass,
            @RequestParam(required=false) String bankVersion,
            @RequestParam(required=false) String status,
            @RequestParam(required=false) String search,
            @RequestParam(defaultValue="20") int limit,
            @RequestParam(defaultValue="0") int offset){
        return noStore(service.list(licenseClass,bankVersion,status,search,limit,offset));
    }

    @GetMapping("/presets/{id}")
    @PreAuthorize("hasRole('ADMIN') and hasAuthority('PERM_practice-exam-sets:read')")
    public ResponseEntity<ApiResponse<Detail>> detail(@PathVariable UUID id){
        return noStore(service.detail(id));
    }

    @PostMapping("/presets")
    @PreAuthorize("hasRole('ADMIN') and hasAuthority('PERM_practice-exam-sets:create')")
    public ResponseEntity<ApiResponse<Detail>> create(
            @AuthenticationPrincipal JwtUserDetails user,@RequestBody Create request){
        return noStore(service.create(request,user.userId()));
    }

    @PostMapping("/presets/{id}/clone")
    @PreAuthorize("hasRole('ADMIN') and hasAuthority('PERM_practice-exam-sets:create')")
    public ResponseEntity<ApiResponse<Detail>> clone(
            @AuthenticationPrincipal JwtUserDetails user,@PathVariable UUID id){
        return noStore(service.cloneVersion(id,user.userId()));
    }

    @PatchMapping("/presets/{id}")
    @PreAuthorize("hasRole('ADMIN') and hasAuthority('PERM_practice-exam-sets:update')")
    public ResponseEntity<ApiResponse<Detail>> edit(
            @AuthenticationPrincipal JwtUserDetails user,@PathVariable UUID id,
            @RequestBody Edit request){
        return noStore(service.edit(id,request,user.userId()));
    }

    @PutMapping("/presets/{id}/questions")
    @PreAuthorize("hasRole('ADMIN') and hasAuthority('PERM_practice-exam-sets:update')")
    public ResponseEntity<ApiResponse<Detail>> questions(
            @AuthenticationPrincipal JwtUserDetails user,@PathVariable UUID id,
            @RequestBody Questions request){
        return noStore(service.questions(id,request,user.userId()));
    }

    @PostMapping("/presets/{id}/validate")
    @PreAuthorize("hasRole('ADMIN') and hasAuthority('PERM_practice-exam-sets:update')")
    public ResponseEntity<ApiResponse<Validation>> validate(
            @AuthenticationPrincipal JwtUserDetails user,@PathVariable UUID id){
        return noStore(service.validate(id,user.userId()));
    }

    @GetMapping("/available-questions")
    @PreAuthorize("hasRole('ADMIN') and hasAuthority('PERM_practice-exam-sets:read')")
    public ResponseEntity<ApiResponse<QuestionPage>> availableQuestions(
            @RequestParam String licenseClass,
            @RequestParam(required=false) Integer chapter,
            @RequestParam(required=false) Integer sourceQuestionId,
            @RequestParam(required=false) String search,
            @RequestParam(defaultValue="50") int limit,
            @RequestParam(defaultValue="0") int offset){
        return noStore(service.availableQuestions(licenseClass,chapter,sourceQuestionId,search,limit,offset));
    }

    @GetMapping("/matrix-policies")
    @PreAuthorize("hasRole('ADMIN') and hasAuthority('PERM_practice-exam-sets:read')")
    public ResponseEntity<ApiResponse<Matrix>> matrix(@RequestParam String licenseClass){
        return noStore(service.matrix(licenseClass));
    }

    private static <T> ResponseEntity<ApiResponse<T>> noStore(T data){
        return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(ApiResponse.ok(data));
    }
}
