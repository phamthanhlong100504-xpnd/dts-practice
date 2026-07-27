# DTS Practice Service - API Documentation

## Overview
DTS Practice Service quản lý ngân hàng câu hỏi sát hạch lái xe (600 câu), các phiên thi thử (Exam Sessions), chấm điểm, tính thời gian, xem lại lịch sử thi và bảng xếp hạng (Leaderboard).

- **Base URL**: `/api/v1`
- **Authentication**: Bearer Token (JWT cấp bởi `dts-identity-service`)
- **Port mặc định**: `8082`

---

## Endpoints

### 1. Questions API (`/api/v1/questions`)

#### `GET /api/v1/questions/chapter/{chapterId}`
Lấy danh sách câu hỏi theo chương (1-6).
- **Headers**: `Authorization: Bearer <token>`
- **Response**: `ApiResponse<List<QuestionResponse>>`

#### `GET /api/v1/questions/critical`
Lấy danh sách các câu hỏi điểm liệt.
- **Headers**: `Authorization: Bearer <token>`
- **Response**: `ApiResponse<List<QuestionResponse>>`

#### `GET /api/v1/questions/stats`
Thống kê tổng số câu hỏi, câu hỏi liệt theo từng hạng bằng.
- **Headers**: `Authorization: Bearer <token>`
- **Response**: `ApiResponse<Map<String, Object>>`

---

### 2. Exam API (`/api/v1/exams`)

#### `POST /api/v1/exams`
Tạo mới một bài thi thử hoặc phiên luyện tập.
- **Headers**: `Authorization: Bearer <token>`
- **Body**:
  ```json
  {
    "examType": "A1",
    "totalQuestions": 25,
    "mode": "EXAM",
    "durationMinutes": 19
  }
  ```
- **Response**: `ApiResponse<ExamSessionResponse>`

#### `GET /api/v1/exams/{examId}`
Lấy thông tin chi tiết phiên thi đang diễn ra.
- **Headers**: `Authorization: Bearer <token>`
- **Response**: `ApiResponse<ExamSessionResponse>`

#### `POST /api/v1/exams/{examId}/answers`
Nộp câu trả lời cho một câu hỏi trong bài thi.
- **Headers**: `Authorization: Bearer <token>`
- **Body**:
  ```json
  {
    "questionId": "101",
    "selectedAnswer": "B"
  }
  ```
- **Response**: `ApiResponse<SubmitAnswerResponse>`

#### `POST /api/v1/exams/{examId}/finish`
Kết thúc bài thi và tính toán kết quả ngay lập tức.
- **Headers**: `Authorization: Bearer <token>`
- **Response**: `ApiResponse<ExamResultResponse>`

#### `GET /api/v1/exams/{examId}/result`
Xem chi tiết kết quả bài thi đã kết thúc.
- **Headers**: `Authorization: Bearer <token>`
- **Response**: `ApiResponse<ExamResultResponse>`

#### `GET /api/v1/exams/history`
Lấy lịch sử thi thử của người dùng hiện tại (hỗ trợ phân trang).
- **Headers**: `Authorization: Bearer <token>`
- **Query Params**: `page` (default 0), `size` (default 10)
- **Response**: `ApiResponse<Page<ExamHistoryResponse>>`

#### `GET /api/v1/exams/leaderboard`
Lấy bảng xếp hạng điểm số thi thử cao nhất.
- **Headers**: `Authorization: Bearer <token>`
- **Query Params**: `limit` (default 10)
- **Response**: `ApiResponse<List<LeaderboardResponse>>`
