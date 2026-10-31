# Hợp đồng quyền Practice bàn giao cho Identity

Task Practice chỉ sửa controller và tài liệu của dts-practice. Không sửa repo Identity, không chạy SQL hoặc nhận thêm task của thành viên khác.

| Permission trong access JWT | Mục đích | Vai trò đề xuất để người phụ trách review |
| --- | --- | --- |
| practice:read | Đọc nội dung và dữ liệu cá nhân đúng scope | STUDENT, TEACHER, ADMIN |
| practice:learn | Học và ôn tập của chính tài khoản | STUDENT, TEACHER, ADMIN |
| practice:exam | Thực hiện phiên thi thử cá nhân | STUDENT, TEACHER, ADMIN |
| practice:import | Import ngân hàng dùng chung, còn bắt buộc ROLE_ADMIN | ADMIN |

Đây là permission mới được đề xuất trong bản sửa. Người phụ trách Identity phải thống nhất vai trò được cấp, tạo file migration mới trên nhánh đã tích hợp đầy đủ chức năng chốt và áp dụng bằng migration trước khi thay đổi database. Không tự chọn số version hoặc sửa migration cũ từ gói Practice.

JWT claim chứa tên quyền thuần; Spring thêm PERM_ ở runtime. Cần đăng nhập/refresh lấy token mới sau migration. Token cũ chỉ có questions:read/exams:read hoặc role không tự được chuyển thành quyền mới; không có bypass để che thiếu permission.

Progress cần practice:read trên Bearer người dùng khi gọi learning-catalog và exam progress của Practice. Nếu học viên dùng cả Progress và Practice, kiểm bộ quyền của cả hai service trước test giao diện.

Chưa coi backend tích hợp hoặc giao diện đã nghiệm thu khi chưa được cấp quyền và chưa đối chiếu Task002/Task004. Chỉ phần code/docs và các test đã chạy có kết quả mới được xác nhận tương ứng.
