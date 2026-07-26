-- V2: Seed 600 cau hoi sat hach lai xe
-- Nguon: Cuc CSGT - Bo Cong an (2025)
BEGIN;

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (1, 1, 'Phần của đường bộ được sử dụng cho phương tiện giao thông đường bộ đi lại là gì?', '[{"label": "A", "text": "Phần mặt đường và lề đường."}, {"label": "B", "text": "Phần đường xe chạy."}, {"label": "C", "text": "Phần đường xe cơ giới."}]'::jsonb, 'B', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (2, 1, 'Làn đường là gì?', '[{"label": "A", "text": "Là một phần của phần đường xe chạy được chia theo chiều dọc của đường, sử dụng cho xe chạy."}, {"label": "B", "text": "Là một phần của phần đường xe chạy được chia theo chiều dọc của đường, có đủ chiều rộng cho xe chạy an toàn."}, {"label": "C", "text": "Là đường cho xe ô tô chạy, dừng, đỗ an toàn."}]'::jsonb, 'B', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (3, 1, 'Khổ giới hạn của đường bộ được hiểu như thế nào là đúng?', '[{"label": "A", "text": "Khổ giới hạn của đường bộ là khoảng trống có kích thước giới hạn về chiều rộng, chiều cao của đường bộ để các xe, bao gồm cả hàng hoá xếp trên xe đi qua được an toàn và được xác định theo quy chuẩn, tiêu chuẩn kỹ thuật của đường bộ."}, {"label": "B", "text": "Là khoảng trống có kích thước giới hạn về chiều rộng của đường, cầu, bến phà, hầm trên đường b ộ để các xe kể cả hàng hóa xếp trên xe đi qua được an toàn."}, {"label": "C", "text": "Là khoảng trống có kích thước giới hạn về chiều cao của cầu, bến phà, hầm trên đường bộ để các xe đi qua được an toàn."}]'::jsonb, 'A', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (4, 1, 'Dải phân cách được lắp đặt để làm gì?', '[{"label": "A", "text": "Để phân chia các làn đường dành cho xe cơ giới và xe thô sơ trên đường cao tốc."}, {"label": "B", "text": "Để phân chia phần đường xe chạy thành hai chiều riêng biệt hoặc để phân chia phần đường dành cho xe cơ giới và xe thô sơ hoặc của nhiều loại xe khác nhau trên cùng một chiều đường."}, {"label": "C", "text": "Để phân tách phần đường xe chạy và hành lang an toàn giao thông."}]'::jsonb, 'B', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (5, 1, 'Vạch kẻ đường là gì?', '[{"label": "A", "text": "Là báo hiệu đường bộ để hỗ trợ cảnh báo nguy hiểm cho người tham gia giao thông đường bộ."}, {"label": "B", "text": "Là vạch chỉ sự phân chia làn đường, vị trí hoặc hướng đi, vị trí dừng lại."}, {"label": "C", "text": "Là báo hiệu cho người tham gia giao thông đường bộ về các thông tin của đường bộ."}, {"label": "D", "text": "Cả ba ý trên."}]'::jsonb, 'B', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (6, 1, 'Người điều khiển phương tiện tham gia giao thông đường bộ được hiểu như thế nào là đúng?', '[{"label": "A", "text": "Là người điều khiển xe cơ giới, người điều khiển xe thô sơ, người điều khiển xe máy chuyên dùng."}, {"label": "B", "text": "Là người được giao nhiệm vụ hướng dẫn giao thông trên đường bộ."}, {"label": "C", "text": "Cả hai ý trên."}]'::jsonb, 'A', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (7, 1, 'Người lái xe được hiểu như thế nào là đúng?', '[{"label": "A", "text": "Là người điều khiển xe cơ giới."}, {"label": "B", "text": "Là người điều khiển xe thô sơ."}, {"label": "C", "text": "Là người điều khiển xe máy chuyên dùng."}]'::jsonb, 'A', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (8, 1, 'Trong nhóm các phương tiện giao thông đường bộ dưới đây, nhóm phương tiện nào là xe cơ giới?', '[{"label": "A", "text": "Xe ô tô; máy kéo; xe mô tô hai bánh; xe mô tô ba bánh; xe gắn máy; xe cơ giới dùng cho người khuyết tật và xe máy chuyên dùng; xe đạp, xe đạp máy, xe đạp điện."}, {"label": "B", "text": "Xe ô tô; rơ moóc được kéo bởi xe ô tô; sơ mi rơ moóc được kéo bởi ô tô đầu kéo; xe chở người bốn bánh có gắn động cơ; xe chở hàng bốn bánh có gắn động cơ; xe mô tô, xe gắn máy và các loại xe tương tự."}]'::jsonb, 'B', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (9, 1, 'Trong nhóm các phương tiện giao thông đường bộ dưới đây, nhóm phương tiện nào là xe thô sơ?', '[{"label": "A", "text": "Xe đạp, xe đạp máy, xe đạp điện; xe xích lô; xe lăn dùng cho người khuyết tật; xe vật nuôi kéo và các loại xe tương tự."}, {"label": "B", "text": "Xe đạp (kể cả xe đạp máy, xe đạp điện), xe gắn máy, xe cơ giới dùng cho người khuyết tật và xe máy chuyên dùng."}, {"label": "C", "text": "Xe ô tô, máy kéo, rơ moóc hoặc sơ mi rơ moóc được kéo bởi xe ô tô, máy kéo."}]'::jsonb, 'A', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (10, 1, 'Phương tiện giao thông đường bộ gồm những loại nào?', '[{"label": "A", "text": "Phương tiện giao thông cơ giới đường bộ."}, {"label": "B", "text": "Phương tiện giao thông thô sơ đường bộ, xe máy chuyên dùng và các loại xe tương tự."}, {"label": "C", "text": "Cả hai ý trên."}]'::jsonb, 'C', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (11, 1, 'Người tham gia giao thông đường bộ gồm những đối tượng nào?', '[{"label": "A", "text": "Người điều khiển, người được chở trên phương tiện tham gia giao thông đường bộ."}, {"label": "B", "text": "Người điều khiển, dẫn dắt vật nuôi trên đường bộ; người đi bộ trên đường bộ."}, {"label": "C", "text": "Cả hai ý trên."}]'::jsonb, 'C', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (12, 1, 'Người điều khiển phương tiện tham gia giao thông đường bộ gồm những đối tượng nào dưới đây?', '[{"label": "A", "text": "Người điều khiển xe cơ giới, người điều khiển xe thô sơ."}, {"label": "B", "text": "Người điều khiển xe máy chuyên dùng."}, {"label": "C", "text": "Cả hai ý trên."}]'::jsonb, 'C', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (13, 1, 'Người điều khiển giao thông đường bộ được hiểu như thế nào là đúng?', '[{"label": "A", "text": "Là người điều khiển phương tiện tham gia giao thông đường bộ."}, {"label": "B", "text": "Là Cảnh sát giao thông và người được giao nhiệm vụ hướng dẫn giao thông trên đường bộ."}, {"label": "C", "text": "Là người tham gia giao thông đường bộ."}]'::jsonb, 'B', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (14, 1, 'Dừng xe được hiểu như thế nào là đúng?', '[{"label": "A", "text": "Là trạng thái đứng yên của xe không giới hạn thời gian để cho người lên, xuống xe, xếp dỡ hàng hóa hoặc thực hiện công việc khác."}, {"label": "B", "text": "Là trạng thái đứng yên tạm thời của xe trong một khoảng thời gian cần thiết đủ để cho người lên xe, xuống xe, xếp dỡ hàng hóa, kiểm tra kỹ thuật xe hoặc hoạt động khác. Khi dừng xe không được tắt máy và không được rời khỏi vị trí lái, trừ trường hợp rời khỏi vị trí lái để đóng, mở cửa xe, xếp dỡ hàng hóa, kiểm tra kỹ thuật xe nhưng phải sử dụng phanh đỗ xe hoặc thực hiện biện pháp an toàn khác."}, {"label": "C", "text": "Là trạng thái đứng yên của xe không giới hạn thời gian giữa 02 lần vận chuyển hàng hóa hoặc hành khách."}]'::jsonb, 'B', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (15, 1, 'Đỗ xe được hiểu như thế nào là đúng?', '[{"label": "A", "text": "Là trạng thái đứng yên của xe có giới hạn thời gian trong một khoảng thời gian cần thiết đủ để cho người lên, xuống xe đó, xếp dỡ hàng hóa hoặc thực hiện công việc khác."}, {"label": "B", "text": "Là trạng thái đứng yên của xe không giới hạn thời gian. Khi đỗ xe, người điều khiển phương tiện tham gia giao thông đường bộ chỉ được rời khỏi xe khi đã sử dụng phanh đỗ xe hoặc thực hiện biện pháp an toàn khác. Xe đỗ trên đoạn đường dốc phải đánh lái về phía lề đường, chèn bánh."}]'::jsonb, 'B', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (16, 1, 'Đường cao tốc được hiểu như thế nào là đúng?', '[{"label": "A", "text": "Là một cấp kỹ thuật của đường bộ, chỉ dành cho một số loại xe cơ giới, xe máy chuyên dùng tham gia giao thông theo quy định của pháp luật. Có dải phân cách phân chia hai chiều xe chạy riêng biệt, không giao nhau cùng mức với một hoặc các đường khác, chỉ cho xe ra, vào ở những điểm nhất định. Có hàng rào bảo vệ, trang thiết bị phục vụ, bảo đảm giao thông liên tục, an toàn, rút ngắn thời gian hành trình."}, {"label": "B", "text": "Là đường nối liền Thủ đô Hà Nội với trung tâm hành chính cấp tỉnh; đường nối liền các trung tâm hành chính cấp tỉnh; đường có vị trí quan trọng đối với sự phát triển kinh tế- xã hội, bảo đảm quốc phòng, an ninh của vùng, khu vực."}]'::jsonb, 'A', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (17, 1, 'Thiết bị an toàn cho trẻ em được hiểu như thế nào là đúng?', '[{"label": "A", "text": "Là thiết bị có đủ khả năng bảo đảm an toàn cho trẻ em ở tư thế ngồi hoặc nằm trên xe ô tô, được thiết kế để giảm nguy cơ chấn thương cho người dùng trong trường hợp xảy ra va chạm hoặc xe ô tô giảm tốc độ đột ngột, bằng cách hạn chế sự di chuyển của cơ thể trẻ em."}, {"label": "B", "text": "Là thiết bị có đủ khả năng bảo đảm an toàn cho trẻ em ở tư thế đứng hoặc nằm trên xe ô tô, được thiết kế để giảm nguy cơ chấn thương cho người dùng trong trường hợp xe ô tô lưu thông bình thường."}]'::jsonb, 'A', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (18, 1, 'Theo chức năng phục vụ thì đường bộ được phân loại như thế nào?', '[{"label": "A", "text": "Đường chính, đường nhánh, đường gom, đường bên, đường dành cho giao thông công cộng, đường nội bộ, đường dành riêng cho người đi bộ, người đi xe đạp và các đường khác."}, {"label": "B", "text": "Đường chính, đường nhánh, đường gom, đường dành cho giao thông công cộng, đường nội bộ, đường dành riêng cho người đi bộ, người đi xe đạp và các đường khác."}, {"label": "C", "text": "Đường chính, đường nhánh, đường gom, đường bên, đường dành cho giao thông công cộng, đường dành riêng cho người đi bộ, người đi xe đạp và các đường khác."}]'::jsonb, 'A', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (19, 1, 'Hành vi nào dưới đây bị nghiêm cấm?', '[{"label": "A", "text": "Sử dụng xe đạp đi trên các tuyến quốc lộ."}, {"label": "B", "text": "Rải vật sắc nhọn, đổ chất gây trơn trượt trên đường bộ."}, {"label": "C", "text": "Cả hai ý trên."}]'::jsonb, 'B', TRUE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (20, 1, 'Hành vi đưa xe cơ giới, xe máy chuyên dùng tham gia giao thông đường bộ nào dưới đây bị cấm?', '[{"label": "A", "text": "Không có chứng nhận kiểm định an toàn kỹ thuật và bảo vệ môi trường."}, {"label": "B", "text": "Hết niên hạn sử dụng."}, {"label": "C", "text": "Cả hai ý trên."}]'::jsonb, 'C', TRUE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (21, 1, 'Tổ chức đua xe được phép thực hiện khi nào?', '[{"label": "A", "text": "Trên đường phố không có người qua lại."}, {"label": "B", "text": "Được người dân ủng hộ."}, {"label": "C", "text": "Được cơ quan có thẩm quyền cấp phép."}]'::jsonb, 'C', TRUE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (22, 1, 'Hành vi đua xe trái phép bị xử lý như thế nào?', '[{"label": "A", "text": "Chỉ bị nhắc nhở."}, {"label": "B", "text": "Tùy theo mức độ của hành vi vi phạm có thể bị xử lý hành chính hoặc xử lý hình sự."}]'::jsonb, 'B', TRUE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (23, 1, 'Hành vi của người điều khiển xe ô tô và các loại xe tương tự khi tham gia giao thông đường bộ mà trong cơ thể có chất ma túy thì bị áp dụng hình thức xử phạt vi phạm hành chính nào dưới đây?', '[{"label": "A", "text": "Bị phạt tiền."}, {"label": "B", "text": "Bị tước giấy phép lái xe."}, {"label": "C", "text": "Cả hai ý trên."}]'::jsonb, 'C', TRUE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (24, 1, 'Người điều khiển phương tiện tham gia giao thông đường bộ mà trong máu hoặc hơi thở có nồng độ cồn có bị nghiêm cấm không?', '[{"label": "A", "text": "Bị nghiêm cấm."}, {"label": "B", "text": "Không bị nghiêm cấm."}, {"label": "C", "text": "Không bị nghiêm cấm, nếu nồng độ cồn trong máu ở mức nhẹ, có thể điều khiển phương tiện tham gia giao thông."}]'::jsonb, 'A', TRUE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (25, 1, 'Hành vi của người điều khiển xe ô tô và các loại xe tương tự khi tham gia giao thông đường bộ mà trong máu hoặc hơi thở có nồng độ cồn thì bị áp dụng hình thức xử phạt vi phạm hành chính nào dưới đây?', '[{"label": "A", "text": "Bị phạt tiền."}, {"label": "B", "text": "Có thể bị tước giấy phép lái xe."}, {"label": "C", "text": "Cả hai ý trên."}]'::jsonb, 'C', TRUE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (26, 1, 'Theo Luật Phòng chống tác hại của rượu, bia, đối tượng nào dưới đây bị cấm sử dụng rượu, bia khi tham gia giao thông?', '[{"label": "A", "text": "Người điều khiển xe ô tô, xe mô tô, xe đạp, xe gắn máy."}, {"label": "B", "text": "Người được chở trên xe cơ giới."}, {"label": "C", "text": "Cả hai ý trên."}]'::jsonb, 'A', TRUE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (27, 1, 'Hành vi giao xe ô tô, mô tô cho người nào sau đây tham gia giao thông đường bộ bị nghiêm cấm?', '[{"label": "A", "text": "Người chưa đủ tuổi theo quy định."}, {"label": "B", "text": "Người không có giấy phép lái xe."}, {"label": "C", "text": "Người có giấy phép lái xe nhưng đã bị trừ hết 12 điểm."}, {"label": "D", "text": "Cả ba ý trên."}]'::jsonb, 'D', TRUE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (28, 1, 'Hành vi nào sau đây bị nghiêm cấm?', '[{"label": "A", "text": "Điều khiển xe cơ giới lạng lách, đánh võng, rú ga liên tục khi tham gia giao thông trên đường."}, {"label": "B", "text": "Xúc phạm, đe dọa, cản trở, chống đối hoặc không chấp hành hiệu lệnh, hướng dẫn, yêu cầu kiểm tra, kiểm soát của người thi hành công vụ về bảo đảm trật tự, an toàn giao thông đường bộ."}, {"label": "C", "text": "Cả hai ý trên."}]'::jsonb, 'C', TRUE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (29, 1, 'Các hành vi nào sau đây bị cấm đối với phương tiện tham gia giao thông đường bộ?', '[{"label": "A", "text": "Cải tạo xe ô tô loại khác thành xe ô tô chở người phục vụ mục đích quốc phòng, an ninh."}, {"label": "B", "text": "Cải tạo trái phép; cố ý can thiệp làm sai lệch chỉ số trên đồng hồ báo quãng đường đã chạy của xe ô tô; cắt, hàn, tẩy xóa, đục sửa, đóng lại trái phép số khung, số động cơ của xe cơ giới, xe máy chuyên dùng."}]'::jsonb, 'B', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (30, 1, 'Hành vi nào sau đây bị cấm?', '[{"label": "A", "text": "Lắp đặt, sử dụng thiết bị âm thanh, ánh sáng trên xe cơ giới, xe máy chuyên dùng gây mất trật tự, an toàn giao thông đường bộ."}, {"label": "B", "text": "Cản trở người, phương tiện tham gia giao thông trên đường bộ; ném gạch, đất, đá, cát hoặc vật thể khác vào người, phương tiện đang tham gia giao thông trên đường bộ."}, {"label": "C", "text": "Cả hai ý trên."}]'::jsonb, 'C', TRUE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (31, 1, 'Việc sản xuất, sử dụng, mua, bán trái phép biển số xe có bị nghiêm cấm hay không?', '[{"label": "A", "text": "Không bị nghiêm cấm."}, {"label": "B", "text": "Bị nghiêm cấm."}, {"label": "C", "text": "Bị nghiêm cấm tuỳ trường hợp."}]'::jsonb, 'B', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (32, 1, 'Khi điều khiển phương tiện tham gia giao thông, những hành vi nào dưới đây bị nghiêm cấm?', '[{"label": "A", "text": "Thay đổi tốc độ của xe nhiều lần."}, {"label": "B", "text": "Điều khiển phương tiện sau 23 giờ trong ngày."}, {"label": "C", "text": "Lạng lách, đánh võng, rú ga liên tục."}]'::jsonb, 'C', TRUE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (33, 1, 'Có bao nhiêu nhóm biển báo hiệu đường bộ?', '[{"label": "A", "text": "Ba nhóm: Biển báo cấm, biển báo nguy hiểm và biển hiệu lệnh."}, {"label": "B", "text": "Bốn nhóm: Biển báo cấm, biển báo nguy hiểm, biển hiệu lệnh và biển phụ."}, {"label": "C", "text": "Năm nhóm: Biển báo cấm, biển báo nguy hiểm, biển hiệu lệnh, biển chỉ dẫn, biển phụ."}]'::jsonb, 'C', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (34, 1, 'Tại nơi có vạch kẻ đường hoặc tại nơi mà người đi bộ, xe lăn của người khuyết tật đang qua đường, người điều khiển phương tiện tham gia giao thông phải thực hiện như thế nào?', '[{"label": "A", "text": "Giảm tốc độ và nhường đường cho người đi bộ, xe lăn của người khuyết tật qua đường đảm bảo an toàn."}, {"label": "B", "text": "Quan sát, giảm tốc độ hoặc dừng lại để bảo đảm an toàn cho người đi bộ, xe lăn của người khuyết tật qua đường."}, {"label": "C", "text": "Quan sát, tăng tốc độ và điều khiển phương tiện nhanh chóng đi qua."}]'::jsonb, 'B', TRUE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (35, 1, 'Người điều khiển xe mô tô phải phải quan sát, giảm tốc độ hoặc dừng lại để bảo đảm an toàn trong các trường hợp nào dưới đây?', '[{"label": "A", "text": "Đường hẹp, đường vòng, đường quanh co, đường đèo, dốc."}, {"label": "B", "text": "Nơi cầu, cống hẹp, đập tràn, đường ngầm, hầm chui, hầm đường bộ."}, {"label": "C", "text": "Trời mưa, gió, sương, khói, bụi, mặt đường trơn trượt, lầy lội, có nhiều đất đá, vật liệu rơi vãi ảnh hưởng đến an toàn giao thông đường bộ."}, {"label": "D", "text": "Cả ba ý trên."}]'::jsonb, 'D', TRUE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (36, 1, 'Khi gặp hiệu lệnh điều khiển của Cảnh sát giao thông như hình dưới đây thì người tham gia giao thông đường bộ phải đi như thế nào là đúng quy tắc giao thông?', '[{"label": "A", "text": "Người tham gia giao thông đường bộ ở các hướng phải dừng lại."}, {"label": "B", "text": "Người tham gia giao thông đường bộ ở các hướng được đi theo chiều gậy chỉ của Cảnh sát giao thông."}, {"label": "C", "text": "Người tham gia giao thông đường bộ ở phía trước và phía sau người điều khiển được đi tất cả các hướng; người tham gia giao thông đường bộ ở phía bên phải và phía bên trái người điều khiển phải dừng lại."}, {"label": "D", "text": "Người tham gia giao thông đường bộ ở phía trước và phía sau người điều khiển phải dừng lại; người tham gia giao thông đường bộ ở phía bên phải và phía bên trái người điều khiển được đi tất cả các hướng."}]'::jsonb, 'D', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (37, 1, 'Khi gặp hiệu lệnh điều khiển của Cảnh sát giao thông như hình dưới đây thì người tham gia giao thông đường bộ phải đi như thế nào là đúng quy tắc giao thông?', '[{"label": "A", "text": "Người tham gia giao thông đường bộ ở phía sau Cảnh sát giao thông được đi, các hướng khác phải dừng lại."}, {"label": "B", "text": "Người tham gia giao thông đường bộ được rẽ phải theo chiều mũi tên màu xanh ở bục Cảnh sát giao thông."}, {"label": "C", "text": "Người tham gia giao thông đường bộ ở tất cả các hướng phải dừng lại, trừ các xe đã ở trong khu vực giao nhau."}, {"label": "D", "text": "Người tham gia giao thông đường bộ ở phía trước Cảnh sát giao thông phải dừng lại, các hướng khác được đi."}]'::jsonb, 'C', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (38, 1, 'Khi hiệu lệnh của người điều khiển giao thông trái với tín hiệu đèn giao thông hoặc biển báo hiệu đường bộ thì người tham gia giao thông đường bộ phải chấp hành báo hiệu đường bộ nào dưới đây?', '[{"label": "A", "text": "Theo hiệu lệnh của người điều khiển giao thông."}, {"label": "B", "text": "Theo tín hiệu đèn giao thông."}, {"label": "C", "text": "Theo biển báo hiệu đường bộ."}]'::jsonb, 'A', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (39, 1, 'Khi ở một vị trí vừa có biển báo hiệu đặt cố định vừa có biển báo hiệu tạm thời mà hai biển có ý nghĩa khác nhau, người tham gia giao thông đường bộ phải chấp hành hiệu lệnh của biển báo hiệu nào?', '[{"label": "A", "text": "Biển báo hiệu đặt cố định."}, {"label": "B", "text": "Biển báo hiệu tạm thời."}, {"label": "C", "text": "Theo quyết định của người tham gia giao thông nhưng phải bảo đảm an toàn."}]'::jsonb, 'B', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (40, 1, 'Tại nơi đường giao nhau, khi đèn điều khiển giao thông có tín hiệu màu vàng, người điều khiển phương tiện tham gia giao thông phải chấp hành như thế nào là đúng quy tắc giao thông?', '[{"label": "A", "text": "Dừng lại trước vạch dừng; trường hợp đang đi trên vạch dừng hoặc đã đi qua vạch dừng mà tín hiệu đèn màu vàng thì được đi tiếp; trường hợp tín hiệu đèn màu vàng nhấp nháy, người điều khiển phương tiện tham gia giao thông đường bộ được đi nhưng phải quan sát, giảm tốc độ hoặc dừng lại nhường đường cho người đi bộ, xe lăn của người khuyết tật qua đường hoặc các phương tiện khác."}, {"label": "B", "text": "Tăng tốc độ nhanh chóng vượt qua nút giao."}, {"label": "C", "text": "Quan sát, giảm tốc độ, từ từ vượt qua nút giao."}]'::jsonb, 'A', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (41, 1, 'Người lái xe trên đường cần chấp hành quy định về tốc độ tối đa như thế nào?', '[{"label": "A", "text": "Chỉ lớn hơn tốc độ tối đa cho phép khi đường vắng."}, {"label": "B", "text": "Chỉ lớn hơn tốc độ tối đa cho phép khi vào ban đêm."}, {"label": "C", "text": "Không vượt quá tốc độ tối đa cho phép."}]'::jsonb, 'C', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (42, 1, 'Khi chở trẻ em dưới 10 tuổi và chiều cao dưới 1,35 mét trên xe ô tô, người lái xe phải thực hiện quy tắc nào dưới đây để bảo đảm an toàn?', '[{"label": "A", "text": "Không được cho trẻ em ngồi cùng hàng ghế với người lái xe, trừ loại xe ô tô chỉ có một hàng ghế; người lái xe phải sử dụng, hướng dẫn sử dụng thiết bị an toàn phù hợp cho trẻ em."}, {"label": "B", "text": "Cho trẻ em ngồi cùng hàng ghế với người lái xe, người lái xe phải sử dụng, hướng dẫn sử dụng thiết bị an toàn phù hợp cho trẻ em."}]'::jsonb, 'A', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (43, 1, 'Phương tiện tham gia giao thông đường bộ di chuyển với tốc độ thấp hơn phải đi như thế nào?', '[{"label": "A", "text": "Đi về bên trái theo chiều đi của mình."}, {"label": "B", "text": "Đi về bên phải theo chiều đi của mình."}, {"label": "C", "text": "Đi ở bất cứ bên nào nhưng phải bấm đèn cảnh báo nguy hiểm để báo hiệu cho các phương tiện khác."}]'::jsonb, 'B', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (44, 1, 'Trên một chiều đường có vạch kẻ phân làn đường, người lái xe cơ giới, xe máy chuyên dùng phải điều khiển xe đi trên làn đường nào?', '[{"label": "A", "text": "Đi trên làn đường bên phải trong cùng."}, {"label": "B", "text": "Đi trên làn đường bên trái."}, {"label": "C", "text": "Đi ở bất cứ làn nào nhưng phải bảo đảm tốc độ cho phép."}]'::jsonb, 'B', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (45, 1, 'Người lái xe phải giảm tốc độ, có tín hiệu rẽ phải và đi sát về bên phải của phần đường xe chạy trong các trường hợp nào dưới đây?', '[{"label": "A", "text": "Khi xe chạy phía trước có tín hiệu vượt xe khác."}, {"label": "B", "text": "Khi phía trước có xe chạy ngược chiều."}, {"label": "C", "text": "Khi xe sau xin vượt nếu đủ điều kiện an toàn."}, {"label": "D", "text": "Khi xe sau có tín hiệu vượt bên phải."}]'::jsonb, 'C', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (46, 1, 'Vượt xe là gì?', '[{"label": "A", "text": "Là tình huống giao thông trên đường mà mỗi chiều đường xe chạy chỉ có một làn đường dành cho xe cơ giới, xe đi phía sau di chuyển sang bên trái để di chuyển lên trước xe phía trước."}, {"label": "B", "text": "Là tình huống giao thông trên đường có từ hai làn đường dành cho xe cơ giới cùng chiều trở lên được phân biệt bằng vạch kẻ đường, xe đi phía sau di chuyển lên trước xe phía trước theo quy tắc sử dụng làn đường."}]'::jsonb, 'A', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (47, 1, 'Người lái xe được phép vượt xe trên cầu hẹp có một làn đường, đường cong có tầm nhìn bị hạn chế hay không?', '[{"label": "A", "text": "Được phép vượt khi đường vắng."}, {"label": "B", "text": "Không được phép vượt."}, {"label": "C", "text": "Được phép vượt khi có việc gấp."}]'::jsonb, 'B', TRUE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (48, 1, 'Muốn vượt xe phía trước, người lái xe mô tô phải có tín hiệu như thế nào dưới đây để bảo đảm an toàn?', '[{"label": "A", "text": "Bấm còi liên tục để xe phía trước biết xe mình xin vượt."}, {"label": "B", "text": "Rú ga liên tục để xe phía trước biết xe mình xin vượt."}, {"label": "C", "text": "Báo hiệu nhấp nháy bằng đèn chiếu sáng phía trước hoặc còi."}]'::jsonb, 'C', TRUE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (49, 1, 'Khi lái xe trong khu đông dân cư, khu vực cơ sở khám bệnh, chữa bệnh trừ các khu vực có biển cấm sử dụng còi, người lái xe được sử dụng còi trong thời gian nào?', '[{"label": "A", "text": "Từ 22 giờ ngày hôm trước đến 05 giờ ngày hôm sau."}, {"label": "B", "text": "Từ 05 giờ đến 22 giờ."}, {"label": "C", "text": "Từ 23 giờ ngày hôm trước đến 05 giờ sáng hôm sau."}]'::jsonb, 'B', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (50, 1, 'Phương tiện tham gia giao thông đường bộ chỉ được sử dụng tín hiệu còi trong các trường hợp nào sau đây?', '[{"label": "A", "text": "Để báo hiệu cho người tham gia giao thông đường bộ khi xuất hiện tình huống có thể mất an toàn giao thông hoặc báo hiệu chuẩn bị vượt xe."}, {"label": "B", "text": "Để báo hiệu cho người tham gia giao thông đường bộ từ 22 giờ ngày hôm trước đến 05 giờ ngày hôm sau trong khu đông dân cư, khu vực cơ sở khám bệnh, chữa bệnh, trừ xe ưu tiên."}, {"label": "C", "text": "Cả hai ý trên."}]'::jsonb, 'A', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (51, 1, 'Người lái xe sử dụng đèn như thế nào khi đi trên các đoạn đường qua khu đông dân cư có hệ thống chiếu sáng đang hoạt động?', '[{"label": "A", "text": "Chỉ bật đèn chiếu xa (đèn pha)."}, {"label": "B", "text": "Bật đèn chiếu xa (đèn pha) khi đường vắng, bật đèn chiếu gần (đèn cốt) khi có xe đi ngược chiều."}, {"label": "C", "text": "Chỉ bật đèn chiếu gần (đèn cốt)."}]'::jsonb, 'C', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (52, 1, 'Khi điều khiển phương tiện tham gia giao thông, hành vi nào sau đây bị cấm?', '[{"label": "A", "text": "Dùng tay cầm và sử dụng điện thoại hoặc thiết bị điện tử khác."}, {"label": "B", "text": "Chỉ được chở người trên thùng xe ô tô chở hàng trong trường hợp chở người đi làm nhiệm vụ cứu nạn, cứu hộ, phòng, chống thiên tai, dịch bệnh hoặc thực hiện nhiệm vụ khẩn cấp."}]'::jsonb, 'A', TRUE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (53, 1, 'Người lái xe không được vượt xe khác khi gặp trường hợp nào dưới đây?', '[{"label": "A", "text": "Trên cầu hẹp có một làn đường; nơi đường giao nhau, đường bộ giao nhau cùng mức với đường sắt; khi gặp xe ưu tiên."}, {"label": "B", "text": "Trên cầu có từ 02 làn xe trở lên."}, {"label": "C", "text": "Trên đường có 02 làn đường được phân chia làn bằng vạch kẻ nét đứt."}]'::jsonb, 'A', TRUE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (54, 1, 'Nơi nào cấm quay đầu xe?', '[{"label": "A", "text": "Ở phần đường dành cho người đi bộ qua đường, trên cầu, đầu cầu, gầm cầu vượt, ngầm."}, {"label": "B", "text": "Tại nơi đường bộ giao nhau cùng mức với đường sắt, đường hẹp, đường dốc, đoạn đường cong tầm nhìn bị che khuất, trên đường cao tốc, trong hầm đường bộ, trên đường một chiều."}, {"label": "C", "text": "Cả hai ý trên."}]'::jsonb, 'C', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (55, 1, 'Người lái xe không được quay đầu xe trong các trường hợp nào dưới đây?', '[{"label": "A", "text": "Ở phần đường dành cho người đi bộ qua đường, trên cầu, đầu cầu, trên đường cao tốc, tại nơi đường bộ giao nhau cùng mức với đường sắt, đường hẹp, đường dốc."}, {"label": "B", "text": "Ở phía trước hoặc phía sau của phần đường dành cho người đi bộ qua đường, trên đường quốc lộ."}, {"label": "C", "text": "Cả hai ý trên."}]'::jsonb, 'A', TRUE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (56, 1, 'Trước khi cho xe chuyển hướng, người lái xe phải làm gì để bảo đảm an toàn giao thông?', '[{"label": "A", "text": "Phải quan sát, bảo đảm khoảng cách an toàn với xe phía sau."}, {"label": "B", "text": "Giảm tốc độ và có tín hiệu báo hướng rẽ."}, {"label": "C", "text": "Chuyển dần sang làn gần nhất với hướng rẽ. Khi bảo đảm an toàn, không gây trở ngại cho người và phương tiện khác mới được chuyển hướng."}, {"label": "D", "text": "Cả ba ý trên."}]'::jsonb, 'D', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (57, 1, 'Khi chuyển làn đường, người lái xe phải bật đèn tín hiệu báo rẽ như thế nào là đúng quy tắc giao thông?', '[{"label": "A", "text": "Khi bắt đầu chuyển làn đường."}, {"label": "B", "text": "Trước khi thay đổi làn đường."}, {"label": "C", "text": "Sau khi thay đổi làn đường."}]'::jsonb, 'B', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (58, 1, 'Người lái xe không được lùi xe ở những khu vực nào dưới đây?', '[{"label": "A", "text": "Ở đường một chiều, khu vực cấm dừng, trên phần đường dành cho người đi bộ qua đường."}, {"label": "B", "text": "Nơi đường bộ giao nhau, đường bộ giao nhau cùng mức với đường sắt."}, {"label": "C", "text": "Nơi tầm nhìn bị che khuất, trong hầm đường bộ, trên đường cao tốc."}, {"label": "D", "text": "Cả ba ý trên."}]'::jsonb, 'D', TRUE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (59, 1, 'Người điều khiển phương tiện tham gia giao thông không được dừng xe, đỗ xe ở những vị trí nào sau đây?', '[{"label": "A", "text": "Trên miệng cống thoát nước, miệng hầm của đường điện thoại, điện cao thế, chỗ dành riêng cho xe chữa cháy lấy nước."}, {"label": "B", "text": "Trong phạm vi an toàn của đường sắt."}, {"label": "C", "text": "Cả hai ý trên."}]'::jsonb, 'C', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (60, 1, 'Trên đường phố, người điều khiển phương tiện tham gia giao thông đường bộ được dừng xe, đỗ xe sát theo lề đường, vỉa hè phía bên phải theo chiều đi của mình; bánh xe gần nhất không được cách xa lề đường, vỉa hè không quá bao nhiêu mét trong các trường hợp dưới đây và không gây cản trở, nguy hiểm cho người và phương tiện tham gia giao thông đường bộ?', '[{"label": "A", "text": "0,25 mét."}, {"label": "B", "text": "0,3 mét."}, {"label": "C", "text": "0,4 mét."}, {"label": "D", "text": "0,5 mét."}]'::jsonb, 'A', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (61, 1, 'Khi dừng, đỗ xe trên đường phố hẹp, người lái xe ô tô phải dừng, đỗ xe ở vị trí cách xe ô tô đang đỗ ngược chiều khoảng cách tối thiểu là bao nhiêu mét trong các trường hợp dưới đây để bảo đảm an toàn?', '[{"label": "A", "text": "10 mét."}, {"label": "B", "text": "15 mét."}, {"label": "C", "text": "20 mét."}, {"label": "D", "text": "25 mét."}]'::jsonb, 'C', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (62, 1, 'Người điều khiển phương tiện tham gia giao thông đường bộ không được dừng xe, đỗ xe tại các vị trí nào sau đây?', '[{"label": "A", "text": "Nơi có lề đường rộng hoặc khu đất ở bên ngoài phần đường xe chạy."}, {"label": "B", "text": "Song song cùng chiều với một xe khác đang dừng, đỗ trên đường; trên đoạn đường cong hoặc gần đầu dốc mà tầm nhìn bị che khuất; bên trái đường một chiều."}, {"label": "C", "text": "Cả hai ý trên."}]'::jsonb, 'B', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (63, 1, 'Người điều khiển xe mô tô hai bánh, xe mô tô ba bánh, xe gắn máy có được phép sử dụng xe để kéo hoặc đẩy các phương tiện khác khi tham gia giao thông không?', '[{"label": "A", "text": "Được phép."}, {"label": "B", "text": "Nếu phương tiện được kéo, đẩy có khối lượng nhỏ hơn phương tiện của mình."}, {"label": "C", "text": "Tùy trường hợp."}, {"label": "D", "text": "Không được phép."}]'::jsonb, 'D', TRUE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (64, 1, 'Khi điều khiển xe mô tô hai bánh, xe mô tô ba bánh, xe gắn máy, những hành vi nào sau đây không được phép?', '[{"label": "A", "text": "Buông cả hai tay; đứng, nằm trên xe điều khiển xe; sử dụng chân chống hoặc vật khác quệt xuống đường khi xe đang chạy."}, {"label": "B", "text": "Chở tối đa hai người phía sau khi chở người bệnh đi cấp cứu, áp giải người có hành vi vi phạm pháp luật, trẻ em dưới 12 tuổi và người già yếu hoặc người khuyết tật."}]'::jsonb, 'A', TRUE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (65, 1, 'Khi điều khiển xe mô tô hai bánh, xe mô tô ba bánh, xe gắn máy, những hành vi nào sau đây không được phép?', '[{"label": "A", "text": "Buông cả hai tay; sử dụng xe để kéo, đẩy xe khác, vật khác; sử dụng chân chống hoặc vật khác quệt xuống đường khi xe đang chạy."}, {"label": "B", "text": "Sử dụng xe để chở người hoặc hàng hóa; để chân chạm xuống đất khi khởi hành."}, {"label": "C", "text": "Đội mũ bảo hiểm; chạy xe đúng tốc độ quy định và chấp hành đúng quy tắc giao thông đường bộ."}, {"label": "D", "text": "Chở người ngồi sau dưới 16 tuổi."}]'::jsonb, 'A', TRUE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (66, 1, 'Người được chở trên xe mô tô hai bánh, xe mô tô ba bánh, xe gắn máy khi tham gia giao thông đường bộ không được thực hiện hành vi nào sau đây?', '[{"label": "A", "text": "Mang, vác vật cồng kềnh."}, {"label": "B", "text": "Bám, kéo hoặc đẩy các phương tiện khác."}, {"label": "C", "text": "Dùng tay cầm điện thoại hoặc các thiết bị điện tử khác."}, {"label": "D", "text": "Ý 1 và ý 2."}]'::jsonb, 'D', TRUE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (67, 1, 'Người được chở trên xe mô tô hai bánh, xe mô tô ba bánh, xe gắn máy khi tham gia giao thông đường bộ có được bám, kéo hoặc đẩy các phương tiện khác không?', '[{"label": "A", "text": "Được phép."}, {"label": "B", "text": "Được bám trong trường hợp phương tiện của mình bị hỏng."}, {"label": "C", "text": "Được kéo, đẩy trong trường hợp phương tiện khác bị hỏng."}, {"label": "D", "text": "Không được phép."}]'::jsonb, 'D', TRUE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (68, 1, 'Người lái xe, người được chở trên xe mô tô hai bánh, xe mô tô ba bánh, xe gắn máy phải thực hiện quy định nào dưới đây?', '[{"label": "A", "text": "Đội mũ bảo hiểm theo đúng quy chuẩn kỹ thuật quốc gia và cài quai đúng quy cách."}, {"label": "B", "text": "Người lái xe phải đội mũ bảo hiểm, người được chở trên xe không nhất thiết phải đội mũ bảo hiểm."}, {"label": "C", "text": "Phải đội mũ bảo hiểm nhưng không nhất thiết phải cài quai."}]'::jsonb, 'A', TRUE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (69, 1, 'Người lái xe mô tô hai bánh, xe gắn máy được phép chở tối đa hai người trong những trường hợp nào?', '[{"label": "A", "text": "Chở người bệnh đi cấp cứu; áp giải người có hành vi vi phạm pháp luật; trẻ em dưới 12 tuổi; người già yếu hoặc người khuyết tật."}, {"label": "B", "text": "Người đã uống rượu, bia; người trong cơ thể có chất ma tuý."}, {"label": "C", "text": "Cả hai ý trên."}]'::jsonb, 'A', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (70, 1, 'Người lái xe mô tô hai bánh, xe mô tô ba bánh, xe gắn máy không được thực hiện các hành vi nào dưới đây?', '[{"label": "A", "text": "Đi xe dàn hàng ngang; buông cả hai tay."}, {"label": "B", "text": "Sử dụng xe để kéo, đẩy xe khác, vật khác, dẫn dắt vật nuôi, mang, vác và chở vật cồng kềnh; chở người đứng trên xe, giá đèo hàng hoặc ngồi trên tay lái; xếp hàng hóa trên xe quá giới hạn quy định."}, {"label": "C", "text": "Ngồi về một bên điều khiển xe; đứng, nằm trên xe điều khiển xe; thay người lái xe khi xe đang chạy; quay người về phía sau để điều khiển xe hoặc bịt mắt điều khiển xe; sử dụng chân chống hoặc vật khác quệt xuống đường khi xe đang chạy."}, {"label": "D", "text": "Cả ba ý trên."}]'::jsonb, 'D', TRUE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (71, 1, 'Người lái xe mô tô hai bánh, xe mô tô ba bánh, xe gắn máy không được thực hiện các hành vi nào sau đây?', '[{"label": "A", "text": "Đi xe dàn hàng ngang; đi xe vào phần đường dành cho người đi bộ và phương tiện khác."}, {"label": "B", "text": "Sử dụng ô, thiết bị âm thanh, trừ thiết bị trợ thính."}, {"label": "C", "text": "Cả hai ý trên."}]'::jsonb, 'C', TRUE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (72, 1, 'Người lái xe mô tô hai bánh, xe mô tô ba bánh, xe gắn máy không được thực hiện hành vi nào sau đây?', '[{"label": "A", "text": "Đi trên phần đường, làn đường quy định, chấp hành hiệu lệnh của người điều khiển giao thông, đèn tín hiệu giao thông."}, {"label": "B", "text": "Đi xe dàn hàng ngang, đi xe vào phần đường dành cho người đi bộ."}, {"label": "C", "text": "Cả hai ý trên."}]'::jsonb, 'B', TRUE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (73, 1, 'Người được chở trên xe mô tô hai bánh, xe mô tô ba bánh, xe gắn máy khi tham gia giao thông đường bộ có được sử dụng ô khi trời mưa hay không?', '[{"label": "A", "text": "Được sử dụng."}, {"label": "B", "text": "Chỉ người ngồi sau được sử dụng."}, {"label": "C", "text": "Không được sử dụng."}, {"label": "D", "text": "Được sử dụng nếu không có áo mưa."}]'::jsonb, 'C', TRUE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (74, 1, 'Người được chở trên xe mô tô có được kéo theo người đang điều khiển xe đạp hay không?', '[{"label": "A", "text": "Chỉ được phép nếu cả hai đội mũ bảo hiểm."}, {"label": "B", "text": "Không được phép."}, {"label": "C", "text": "Chỉ được thực hiện trên đường vắng."}]'::jsonb, 'B', TRUE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (75, 1, 'Trường hợp người được chở trên xe mô tô, xe gắn máy, các loại xe tương tự xe mô tô và các loại xe tương tự xe gắn máy không đội "mũ bảo hiểm cho người đi mô tô, xe máy" hoặc không cài quai đúng quy cách (trừ trường hợp chở người bệnh đi cấp cứu, trẻ em dưới 06 tuổi, áp giải người có hành vi vi phạm pháp luật) thì việc xử phạt vi phạm hành chính được quy định như thế nào?', '[{"label": "A", "text": "Không bị xử phạt chỉ bị nhắc nhở."}, {"label": "B", "text": "Người được chở không bị xử phạt, chỉ xử phạt người điều khiển xe mô tô, xe gắn máy."}, {"label": "C", "text": "Người được chở bị xử phạt, không xử phạt người điều khiển xe mô tô, xe gắn máy."}, {"label": "D", "text": "Xử phạt cả người điều khiển và người được chở trên xe mô tô, xe gắn máy."}]'::jsonb, 'D', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (76, 1, 'Trong các trường hợp dưới đây, để bảo đảm an toàn khi tham gia giao thông, người lái xe mô tô cần thực hiện như thế nào?', '[{"label": "A", "text": "Phải đội mũ bảo hiểm theo đúng quy chuẩn kỹ thuật quốc gia và cài quai đúng quy cách, không sử dụng ô, điện thoại di động, thiết bị âm thanh (trừ thiết bị trợ thính)."}, {"label": "B", "text": "Phải đội mũ bảo hiểm khi trời mưa gió hoặc trời quá nắng; có thể sử dụng ô, điện thoại di động, thiết bị âm thanh nhưng phải bảo đảm an toàn."}, {"label": "C", "text": "Phải đội mũ bảo hiểm khi cảm thấy mất an toàn giao thông hoặc khi chuẩn bị di chuyển quãng đường xa."}]'::jsonb, 'A', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (77, 1, 'Thứ tự xuống phà như thế nào là đúng quy tắc giao thông?', '[{"label": "A", "text": "Xe thô sơ, người đi bộ xuống trước, xe cơ giới, xe máy chuyên dùng xuống sau."}, {"label": "B", "text": "Xe cơ giới, xe máy chuyên dùng xuống trước, xe thô sơ, người đi bộ xuống sau."}, {"label": "C", "text": "Xe cơ giới, xe thô sơ xuống trước, xe máy chuyên dùng, người đi bộ xuống sau."}]'::jsonb, 'B', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (78, 1, 'Trên đường có nhiều làn đường cho xe đi cùng chiều được phân biệt bằng vạch kẻ phân làn đường, người điều khiển phương tiện tham gia giao thông đường bộ phải cho xe đi như thế nào?', '[{"label": "A", "text": "Cho xe đi trên bất kỳ làn đường nào hoặc giữa 02 làn đường nếu không có xe đi phía trước; khi cần thiết phải chuyển làn đường, người lái xe phải quan sát xe phía trước để bảo đảm an toàn."}, {"label": "B", "text": "Phải cho xe đi trong một làn đường và chỉ được chuyển làn đường ở những nơi cho phép; mỗi lần chuyển làn đường chỉ được phép chuyển sang một làn đường 16 liền kề; khi chuyển làn đường phải có tín hiệu báo trước; phải quan sát bảo đảm khoảng cách an toàn với xe phía trước, phía sau và hai bên mới được chuyển làn."}, {"label": "C", "text": "Phải cho xe đi trong một làn đường, chỉ được chuyển làn đường khi vượt xe phía trước cùng làn đường."}]'::jsonb, 'B', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (79, 1, 'Trên đường một chiều có vạch kẻ phân làn đường, xe thô sơ và xe cơ giới phải đi như thế nào là đúng quy tắc giao thông?', '[{"label": "A", "text": "Xe thô sơ phải đi trên làn đường bên trái, xe cơ giới, xe máy chuyên dùng đi trên làn đường bên phải trong cùng."}, {"label": "B", "text": "Xe thô sơ phải đi trên làn đường bên phải trong cùng, xe cơ giới, xe máy chuyên dùng đi trên làn đường bên trái."}, {"label": "C", "text": "Xe thô sơ phải đi trên làn đường bên phải trong cùng, xe cơ giới, xe máy chuyên dùng được đi trên tất cả các làn đường."}]'::jsonb, 'B', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (80, 1, 'Khi lái xe trong đô thị và khu đông dân cư trong thời gian từ 22 giờ ngày hôm trước đến 05 giờ ngày hôm sau, nếu cần vượt một xe khác, người lái xe phải báo hiệu như thế nào là đúng quy tắc giao thông?', '[{"label": "A", "text": "Chỉ được báo hiệu bằng còi."}, {"label": "B", "text": "Phải báo hiệu bằng cả còi và đèn."}, {"label": "C", "text": "Chỉ được báo hiệu bằng đèn."}]'::jsonb, 'C', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (81, 1, 'Khi điều khiển xe chạy trên đường, biết có xe sau xin vượt, nếu đủ điều kiện an toàn người điều khiển phương tiện phải làm gì?', '[{"label": "A", "text": "Tăng tốc độ và ra hiệu cho xe sau vượt, không được gây trở ngại cho xe xin vượt."}, {"label": "B", "text": "Giảm tốc độ, có tín hiệu rẽ phải để báo hiệu cho người điều khiển phương tiện tham gia giao thông đường bộ phía sau biết được vượt và đi sát về bên phải của phần đường xe chạy cho đến khi xe sau đã vượt qua, không được cản trở đối với xe xin vượt."}, {"label": "C", "text": "Cho xe đi sát về bên trái của phần đường xe chạy và ra hiệu cho xe sau vượt, không được gây trở ngại cho xe xin vượt."}]'::jsonb, 'B', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (82, 1, 'Khi điều khiển xe chạy trên đường, biết có xe sau xin vượt, nếu không đủ điều kiện an toàn người điều khiển phương tiện phải làm gì?', '[{"label": "A", "text": "Có tín hiệu rẽ trái để báo hiệu cho người điều khiển phương tiện tham gia giao thông đường bộ phía sau biết là chưa được vượt."}, {"label": "B", "text": "Tiếp tục đi cho đến khi đủ điều kiện an toàn, nếu xe sau vẫn xin vượt thì báo hiệu cho xe sau vượt."}, {"label": "C", "text": "Tăng tốc độ để báo hiệu cho xe xin vượt biết là chưa được vượt."}]'::jsonb, 'A', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (83, 1, 'Khi chuyển hướng, người lái xe, người điều khiển xe máy chuyên dùng phải làm gì để bảo đảm an toàn?', '[{"label": "A", "text": "Phải nhường đường cho người đi bộ, xe thô sơ, xe đi ngược chiều và chỉ chuyển hướng khi không gây trở ngại hoặc nguy hiểm cho người, phương tiện khác."}, {"label": "B", "text": "Nhanh chóng chuyển hướng để không gây trở ngại hoặc nguy hiểm cho người, phương tiện khác."}, {"label": "C", "text": "Ra tín hiệu chuyển hướng để yêu cầu các phương tiện khác dừng lại cho xe chuyển hướng di chuyển."}]'::jsonb, 'A', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (84, 1, 'Trước khi chuyển hướng, người điều khiển phương tiện tham gia giao thông đường bộ phải thực hiện như thế nào để bảo đảm an toàn?', '[{"label": "A", "text": "Quan sát gương, nếu không có phương tiện gần hướng rẽ thì nhanh chóng chuyển hướng."}, {"label": "B", "text": "Quan sát, bảo đảm khoảng cách an toàn với xe phía sau, giảm tốc độ và có tín hiệu báo hướng rẽ hoặc có tín hiệu bằng tay theo hướng rẽ đối với xe thô sơ không có đèn báo hướng rẽ, chuyển dần sang làn gần nhất với hướng rẽ. Tín hiệu báo hướng rẽ hoặc tín hiệu bằng tay phải sử dụng liên tục trong quá trình chuyển hướng. Khi bảo đảm an toàn, không gây trở ngại cho người và phương tiện khác mới được chuyển hướng."}, {"label": "C", "text": "Quan sát gương, tăng tốc độ, ra tín hiệu và chuyển hướng."}]'::jsonb, 'B', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (85, 1, 'Khi lùi xe, người điều khiển phương tiện tham gia giao thông đường bộ phải làm gì để bảo đảm an toàn?', '[{"label": "A", "text": "Quan sát phía sau và cho lùi xe ở tốc độ chậm."}, {"label": "B", "text": "Có tín hiệu báo lùi, nếu phía sau không có người hoặc phương tiện khác thì nhanh chóng lùi để bảo đảm an toàn."}, {"label": "C", "text": "Phải quan sát hai bên và phía sau xe, có tín hiệu lùi và chỉ lùi xe khi bảo đảm an toàn."}]'::jsonb, 'C', TRUE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (86, 1, 'Khi muốn lùi xe ở nơi có tầm nhìn bị che khuất, người lái xe phải xử lý như thế nào để đảm bảo an toàn?', '[{"label": "A", "text": "Phải lùi thật chậm."}, {"label": "B", "text": "Bấm còi 3 lần liên tiếp trước khi lùi."}, {"label": "C", "text": "Không được lùi xe."}]'::jsonb, 'C', TRUE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (87, 1, 'Trên đường không phân chia thành hai chiều xe chạy riêng biệt, người điều khiển phương tiện tham gia giao thông đường bộ phải tránh xe đi ngược chiều như thế nào để bảo đảm an toàn?', '[{"label": "A", "text": "Giảm tốc độ và cho xe đi về bên phải theo chiều xe chạy của mình."}, {"label": "B", "text": "Một trong hai xe phải dừng lại cho xe kia đi qua mới được đi."}, {"label": "C", "text": "Tăng tốc độ, cho xe đi về bên phải theo chiều xe chạy của mình để nhanh chóng vượt qua."}]'::jsonb, 'A', TRUE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (88, 1, 'Khi tránh xe đi ngược chiều, các xe phải nhường đường như thế nào là đúng quy tắc giao thông?', '[{"label": "A", "text": "Nơi đường hẹp chỉ đủ cho một xe chạy và có chỗ tránh xe thì xe nào ở gần chỗ tránh hơn phải vào vị trí tránh, nhường đường cho xe đi ngược chiều."}, {"label": "B", "text": "Xe xuống dốc phải nhường đường cho xe lên dốc."}, {"label": "C", "text": "Xe có chướng ngại vật phía trước phải nhường đường cho xe không có chướng ngại vật phía trước."}, {"label": "D", "text": "Cả ba ý trên."}]'::jsonb, 'D', TRUE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (89, 1, 'Khi đang lái xe xuống dốc gặp một xe đang đi lên dốc, người lái xe phải làm gì?', '[{"label": "A", "text": "Tiếp tục đi và phát tín hiệu cho xe lên dốc nhường đường cho xe bạn."}, {"label": "B", "text": "Nhường đường cho xe lên dốc."}, {"label": "C", "text": "Chỉ nhường đường khi xe lên dốc nháy đèn."}]'::jsonb, 'B', TRUE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (90, 1, 'Người lái xe phải làm gì để bảo đảm an toàn khi lái xe trên đường cong có tầm nhìn bị hạn chế?', '[{"label": "A", "text": "Quan sát, giảm tốc độ hoặc dừng lại để bảo đảm an toàn."}, {"label": "B", "text": "Đi sang làn đường của xe ngược chiều để mở rộng tầm nhìn và vượt xe khác."}, {"label": "C", "text": "Cho xe đi sát bên phải làn đường, bật tín hiệu báo hiệu để vượt bên phải xe khác."}]'::jsonb, 'A', TRUE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (91, 1, 'Tại nơi đường giao nhau, người lái xe đang đi trên đường không ưu tiên, đường nhánh phải nhường đường như thế nào là đúng quy tắc giao thông?', '[{"label": "A", "text": "Nhường đường cho xe đi ở bên phải mình tới."}, {"label": "B", "text": "Nhường đường cho xe đi ở bên trái mình tới."}, {"label": "C", "text": "Nhường đường cho xe đi trên đường ưu tiên hoặc đường chính từ bất kỳ hướng nào tới."}]'::jsonb, 'C', TRUE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (92, 1, 'Tại nơi đường giao nhau có báo hiệu đi theo vòng xuyến, người lái xe phải nhường đường như thế nào là đúng quy tắc giao thông?', '[{"label": "A", "text": "Nhường đường cho xe đi đến từ bên phải."}, {"label": "B", "text": "Nhường đường cho xe đi đến từ bên trái."}, {"label": "C", "text": "Không phải nhường đường."}]'::jsonb, 'B', TRUE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (93, 1, 'Tại nơi đường giao nhau không có báo hiệu đi theo vòng xuyến, người điều khiển phương tiện phải nhường đường như thế nào là đúng quy tắc giao thông?', '[{"label": "A", "text": "Phải nhường đường cho xe đi đến từ bên phải."}, {"label": "B", "text": "Xe báo hiệu xin đường trước, xe đó được đi trước."}, {"label": "C", "text": "Phải nhường đường cho xe đi đến từ bên trái."}]'::jsonb, 'A', TRUE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (94, 1, 'Người lái xe phải nhanh chóng giảm tốc độ, đi sát lề đường bên phải hoặc dừng lại để nhường đường cho các loại xe nào dưới đây?', '[{"label": "A", "text": "Xe chữa cháy của Cảnh sát phòng cháy, chữa cháy và cứu nạn, cứu hộ và xe chữa cháy của các lực lượng khác; xe của lực lượng quân sự, công an và kiểm sát; đoàn xe có xe Cảnh sát giao thông dẫn đường; xe cứu thương; xe hộ đê không có tín hiệu ưu tiên theo quy định."}, {"label": "B", "text": "Xe ưu tiên gồm xe chữa cháy của Cảnh sát phòng cháy, chữa cháy và cứu nạn, cứu hộ và xe chữa cháy của các lực lượng khác được huy động đi làm nhiệm vụ chữa cháy; xe của lực lượng quân sự, công an và kiểm sát đi làm nhiệm vụ khẩn cấp; đoàn xe có xe Cảnh sát giao thông dẫn đường; xe cứu thương đi làm nhiệm vụ cấp cứu; xe hộ đê đi làm nhiệm vụ; xe đi làm nhiệm vụ cứu nạn, cứu hộ, khắc phục sự cố thiên tai, dịch bệnh hoặc xe đi làm nhiệm vụ trong tình trạng khẩn cấp theo quy định của pháp luật; đoàn xe tang."}, {"label": "C", "text": "Xe ô tô, xe máy, đoàn xe đang diễu hành có tổ chức có báo tín hiệu xin vượt bằng còi và đèn."}]'::jsonb, 'B', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (95, 1, 'Xe ưu tiên (trừ đoàn xe tang) tham gia giao thông đường bộ khi đi làm nhiệm vụ được quyền ưu tiên nào dưới đây?', '[{"label": "A", "text": "Không bị hạn chế tốc độ."}, {"label": "B", "text": "Được phép đi không phụ thuộc vào tín hiệu đèn giao thông, đi vào đường ngược chiều, các đường khác có thể đi được."}, {"label": "C", "text": "Được đi ngược chiều trên làn dừng xe khẩn cấp trên đường cao tốc."}, {"label": "D", "text": "Cả ba ý trên."}]'::jsonb, 'D', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (96, 1, 'Khi có tín hiệu của xe ưu tiên, người và phương tiện tham gia giao thông đường bộ phải tuân thủ quy định nào dưới đây?', '[{"label": "A", "text": "Giảm tốc độ, đi sát lề đường bên phải hoặc dừng lại để nhường đường."}, {"label": "B", "text": "Tăng tốc độ và đi sát lề đường bên phải để nhường đường."}, {"label": "C", "text": "Giảm tốc độ, đi sát lề đường bên trái để nhường đường."}]'::jsonb, 'A', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (97, 1, 'Khi đang lái xe, phía trước có một xe Cảnh sát giao thông không phát tín hiệu ưu tiên, người lái xe có được phép vượt hay không?', '[{"label": "A", "text": "Không được vượt."}, {"label": "B", "text": "Được phép vượt ở phần đường dành cho người đi bộ qua đường."}, {"label": "C", "text": "Được vượt khi bảo đảm an toàn."}]'::jsonb, 'C', TRUE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (98, 1, 'Khi đang lái xe, phía trước có một xe cứu thương đang phát tín hiệu ưu tiên, người lái xe có được phép vượt hay không?', '[{"label": "A", "text": "Không được vượt."}, {"label": "B", "text": "Được vượt khi đang đi trên cầu."}, {"label": "C", "text": "Được phép vượt khi đi qua nơi giao nhau có ít phương tiện cùng tham gia giao thông."}, {"label": "D", "text": "Được vượt khi bảo đảm an toàn."}]'::jsonb, 'A', TRUE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (99, 1, 'Khi tới đường ngang không có người gác, chắn đường bộ, chuông, đèn tín hiệu, người tham gia giao thông đường bộ phải làm gì để bảo đảm an toàn?', '[{"label": "A", "text": "Dừng lại về bên phải đường của mình, trước vạch dừng xe và quan sát hai phía, khi không có phương tiện giao thông đường sắt tới mới được đi qua."}, {"label": "B", "text": "Quan sát hai phía, khi không có phương tiện giao thông đường sắt tới thì nhanh chóng đi qua."}, {"label": "C", "text": "Dừng lại khoảng cách tối thiểu 3 mét tính từ ray đường sắt gần nhất, khi không có phương tiện giao thông đường sắt tới thì nhanh chóng đi qua."}]'::jsonb, 'A', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (100, 1, 'Tại đường ngang, cầu chung đường sắt, khi có hiệu lệnh của nhân viên gác chắn, đèn đỏ sáng nhấp nháy, chuông kêu, chắn đường bộ đang dịch chuyển hoặc đã đóng, người tham gia giao thông đường bộ phải làm gì để bảo đảm an toàn?', '[{"label": "A", "text": "Dừng lại về bên trái đường của mình, trước vạch dừng xe."}, {"label": "B", "text": "Dừng lại giữa đường của mình, trước vạch dừng xe."}, {"label": "C", "text": "Dừng lại về bên phải đường của mình, trước vạch dừng xe."}]'::jsonb, 'C', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (101, 1, 'Khi xe ô tô bị hư hỏng, bị tai nạn trên đường ngang, cầu chung đường sắt mà không thể di chuyển ngay khỏi phạm vi an toàn đường sắt, người lái xe và người có mặt xử lý như thế nào là đúng quy định?', '[{"label": "A", "text": "Người lái xe và người có mặt phải ngay lập tức báo hiệu để dừng tàu, thực hiện các biện pháp bảo đảm an toàn."}, {"label": "B", "text": "Để xe lại, tìm cách báo cho người quản lý đường sắt, nhà ga nơi gần nhất."}, {"label": "C", "text": "Không di chuyển phương tiện, tự khắc phục sửa chữa."}]'::jsonb, 'A', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (102, 1, 'Người tham gia giao thông đường bộ phải dừng lại về bên phải đường của mình trước vạch dừng xe tại đường ngang, cầu chung đường sắt khi có báo hiệu nào dưới đây?', '[{"label": "A", "text": "Hiệu lệnh của nhân viên gác chắn."}, {"label": "B", "text": "Đèn đỏ sáng nhấp nháy, chuông kêu."}, {"label": "C", "text": "Chắn đường bộ đang dịch chuyển hoặc đã đóng."}, {"label": "D", "text": "Cả ba ý trên."}]'::jsonb, 'D', TRUE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (103, 1, 'Người điều khiển phương tiện tham gia giao thông trong hầm đường bộ ngoài việc phải tuân thủ các quy tắc giao thông còn phải thực hiện những quy định nào dưới đây?', '[{"label": "A", "text": "Xe cơ giới, xe máy chuyên dùng phải bật đèn chiếu gần; xe thô sơ phải bật đèn hoặc có vật phát sáng báo hiệu; không dừng xe, đỗ xe trong hầm đường bộ; trường hợp gặp sự cố kỹ thuật hoặc bất khả kháng khác buộc phải dừng xe, đỗ xe, người lái xe, người điều khiển xe máy chuyên dùng phải đưa xe vào vị trí dừng xe, đỗ xe khẩn cấp, nếu không di chuyển được, phải có báo hiệu bằng đèn khẩn cấp và đặt biển hoặc đèn cảnh báo về phía sau xe khoảng cách bảo đảm an toàn."}, {"label": "B", "text": "Xe cơ giới, xe máy chuyên dùng phải bật đèn chiếu xa; được dừng xe, đỗ xe khi cần thiết."}, {"label": "C", "text": "Phải cho xe chạy trên một làn đường và chỉ chuyển làn ở nơi được phép; được quay đầu xe, lùi xe khi cần thiết."}]'::jsonb, 'A', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (104, 1, 'Việc nối giữa xe kéo với xe được kéo trong trường hợp hệ thống hãm của xe được kéo không còn hiệu lực thì xe kéo nhau phải nối bằng cách nào?', '[{"label": "A", "text": "Nối bằng dây cáp có độ dài 6 mét."}, {"label": "B", "text": "Nối bằng dây cáp có độ dài 8 mét."}, {"label": "C", "text": "Nối bằng thanh nối cứng."}]'::jsonb, 'C', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (105, 1, 'Xe kéo rơ moóc, xe ô tô đầu kéo khi tham gia giao thông đường bộ phải tuân thủ quy tắc giao thông nào ghi ở dưới đây?', '[{"label": "A", "text": "Chỉ được kéo rơ moóc, sơ mi rơ moóc phù hợp với thiết kế của xe; việc kết nối xe kéo với rơ moóc, xe ô tô đầu kéo với sơ mi rơ moóc phải bảo đảm chắc chắn, an toàn."}, {"label": "B", "text": "Phải có tổng trọng lượng tương đương tổng trọng lượng của rơ moóc hoặc phải có hệ thống hãm có hiệu lực cho rơ moóc."}, {"label": "C", "text": "Phải được lắp phanh phụ theo quy định để đảm bảo an toàn."}]'::jsonb, 'A', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (106, 1, 'Luật Trật tự, an toàn giao thông đường bộ quy định về xe kéo xe, xe kéo rơ moóc và xe ô tô đầu kéo kéo sơ mi rơ moóc như thế nào dưới đây?', '[{"label": "A", "text": "Một xe ô tô được kéo theo nhiều xe ô tô hoặc xe máy chuyên dùng khác khi xe được kéo không tự chạy được (trừ trường hợp xe vận chuyển hàng siêu trường, siêu trọng)."}, {"label": "B", "text": "Xe được kéo phải có người điều khiển và hệ thống lái của xe đó phải còn hiệu lực; việc nối xe kéo với xe được kéo phải bảo đảm chắc chắn, an toàn; trường hợp hệ thống hãm của xe được kéo không còn hiệu lực thì xe kéo nhau phải nối bằng thanh nối cứng. Phía trước của xe kéo và phía sau của xe được kéo phải có biển báo hiệu, có đèn cảnh báo nhấp nháy màu vàng."}, {"label": "C", "text": "Cả hai ý trên."}]'::jsonb, 'B', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (107, 1, 'Người điều khiển phương tiện tham gia giao thông đường bộ phải quan sát, giảm tốc độ hoặc dừng lại để bảo đảm an toàn trong các trường hợp nào dưới đây?', '[{"label": "A", "text": "Có báo hiệu cảnh báo nguy hiểm hoặc có chướng ngại vật trên đường; chuyển hướng xe chạy hoặc tầm nhìn bị hạn chế."}, {"label": "B", "text": "Nơi cầu, cống hẹp, đập tràn, đường ngầm, hầm chui, hầm đường bộ; có vật nuôi đi trên đường hoặc chăn thả ở ven đường."}, {"label": "C", "text": "Điểm dừng xe, đỗ xe trên đường bộ có khách đang lên, xuống xe."}, {"label": "D", "text": "Cả ba ý trên."}]'::jsonb, 'D', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (108, 1, 'Trên đường mà mỗi chiều đường xe chạy chỉ có một làn đường dành cho xe cơ giới, người lái xe không được vượt xe khác trong trường hợp nào dưới đây?', '[{"label": "A", "text": "Có chướng ngại vật phía trước, xe chạy phía trước có tín hiệu vượt xe khác."}, {"label": "B", "text": "Xe chạy phía trước đã có tín hiệu rẽ phải và tránh về bên phải."}, {"label": "C", "text": "Cả hai ý trên."}]'::jsonb, 'A', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (109, 1, 'Người lái xe được phép vượt xe khác về bên phải trong trường hợp nào dưới đây?', '[{"label": "A", "text": "Xe phía trước có tín hiệu rẽ trái hoặc đang rẽ trái hoặc khi xe chuyên dùng đang làm việc trên đường mà không thể vượt bên trái."}, {"label": "B", "text": "Xe phía trước đang đi sát lề đường bên trái."}, {"label": "C", "text": "Cả hai ý trên."}]'::jsonb, 'A', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (110, 1, 'Khi có xe xin vượt, người lái xe mô tô xử lý như thế nào nếu đủ điều kiện an toàn cho xe phía sau vượt?', '[{"label": "A", "text": "Giảm tốc độ, có tín hiệu rẽ phải để báo hiệu cho người điều khiển phương tiện tham gia giao thông đường bộ phía sau biết được vượt và đi sát về bên phải của phần đường xe chạy cho đến khi xe sau đã vượt qua, không được cản trở đối với xe xin vượt."}, {"label": "B", "text": "Lái xe vào lề đường bên trái và giảm tốc độ để xe phía sau vượt qua, không được gây trở ngại đối với xe xin vượt."}, {"label": "C", "text": "Tăng tốc độ, đi sát về bên phải của phần đường xe chạy cho đến khi xe sau đã vượt qua."}]'::jsonb, 'A', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (111, 1, 'Những trường hợp nào dưới đây không được đi trên đường cao tốc, trừ người, phương tiện giao thông đường bộ và thiết bị phục vụ việc quản lý, bảo trì đường cao tốc?', '[{"label": "A", "text": "Xe máy chuyên dùng có tốc độ thiết kế nhỏ hơn tốc độ tối thiểu quy định đối với đường cao tốc, xe chở người bốn bánh có gắn động cơ, xe chở hàng bốn bánh có gắn động cơ, xe mô tô, xe gắn máy, các loại xe tương tự xe mô tô, xe gắn máy, xe thô sơ, người đi bộ."}, {"label": "B", "text": "Xe máy chuyên dùng có tốc độ thiết kế lớn hơn tốc độ tối thiểu quy định đối với đường cao tốc."}, {"label": "C", "text": "Xe ô tô và xe máy chuyên dùng có tốc độ thiết kế lớn hơn 80 km/h."}]'::jsonb, 'A', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (112, 1, 'Khi tham gia giao thông trên đường cao tốc, xe ưu tiên đi làm nhiệm vụ khẩn cấp được đi ngược chiều trong trường hợp nào dưới đây?', '[{"label": "A", "text": "Được đi ngược chiều bất cứ làn đường nào của đường cao tốc có thể đi được."}, {"label": "B", "text": "Chỉ được đi ngược chiều trên làn dừng xe khẩn cấp."}, {"label": "C", "text": "Chỉ được đi ngược chiều trên làn đường sát dải phân cách của đường cao tốc."}]'::jsonb, 'B', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (113, 1, 'Khi tham gia giao thông trên đường cao tốc, người lái xe không được thực hiện hành vi nào sau đây?', '[{"label": "A", "text": "Dừng, đỗ xe trên phần đường xe chạy, trừ trường hợp xe không thể di chuyển được vào làn đường khẩn cấp."}, {"label": "B", "text": "Lùi xe, quay đầu xe."}, {"label": "C", "text": "Cả hai ý trên."}]'::jsonb, 'C', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (114, 1, 'Khi xe gặp sự cố kỹ thuật trên đường cao tốc, bạn phải xử lý như thế nào để bảo đảm an toàn giao thông?', '[{"label": "A", "text": "Bật đèn tín hiệu khẩn cấp, dừng xe ngay lập tức và đặt biển báo hiệu nguy hiểm để cảnh báo cho các xe khác."}, {"label": "B", "text": "Bật tín hiệu khẩn cấp, lập tức đưa xe vào làn đường xe chạy bên phải trong cùng, đặt biển báo hiệu nguy hiểm để cảnh báo cho các xe khác."}, {"label": "C", "text": "Dừng xe, đỗ xe ở làn dừng khẩn cấp cùng chiều xe chạy và phải có báo hiệu bằng đèn khẩn cấp; trường hợp xe không thể di chuyển được vào làn dừng khẩn cấp, phải có báo hiệu bằng đèn khẩn cấp và đặt biển hoặc đèn cảnh báo về phía sau xe khoảng cách tối thiểu 150 mét, nhanh chóng báo cho cơ quan Cảnh sát giao thông thực hiện nhiệm vụ bảo đảm trật tự, an toàn giao thông trên tuyến hoặc cơ quan quản lý đường cao tốc."}]'::jsonb, 'C', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (115, 1, 'Trên đường cao tốc, người lái xe xử lý như thế nào khi đã vượt quá lối ra của đường định rẽ?', '[{"label": "A", "text": "Quay đầu xe, chạy trên lề đường có lối ra và rẽ khỏi đường cao tốc."}, {"label": "B", "text": "Lùi xe trên lề đường có lối ra và rẽ khỏi đường cao tốc."}, {"label": "C", "text": "Tiếp tục lái xe và rẽ ở lối ra tiếp theo."}]'::jsonb, 'C', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (116, 1, 'Khi xảy ra ùn tắc trên đường cao tốc có làn dừng xe khẩn cấp, người lái xe có được cho xe chạy ở làn dừng xe khẩn cấp để nhanh chóng thoát khỏi khu vực ùn tắc không (trừ xe ưu tiên)?', '[{"label": "A", "text": "Có."}, {"label": "B", "text": "Không."}]'::jsonb, 'B', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (117, 1, 'Khi chuẩn bị nhập vào làn đường của đường cao tốc, người lái xe, người điều khiển xe máy chuyên dùng phải thực hiện như thế nào là đúng quy tắc giao thông?', '[{"label": "A", "text": "Có tín hiệu xin vào và phải nhường đường cho xe đang chạy trên đường."}, {"label": "B", "text": "Quan sát xe phía sau bảo đảm khoảng cách an toàn mới cho xe nhập vào làn đường sát bên phải."}, {"label": "C", "text": "Nếu có làn đường tăng tốc thì phải cho xe chạy trên làn đường đó trước khi nhập vào làn đường của đường cao tốc."}, {"label": "D", "text": "Cả ba ý trên."}]'::jsonb, 'D', TRUE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (118, 1, 'Theo quy định về độ tuổi, người đủ bao nhiêu tuổi trở lên thì được cấp giấy phép lái xe ô tô tải và ô tô chuyên dùng có khối lượng toàn bộ theo thiết kế trên 3.500 kg đến 7.500 kg; các loại xe ô tô tải quy định cho giấy phép lái xe hạng C1 kéo rơ moóc có khối lượng toàn bộ theo thiết kế đến 750 kg?', '[{"label": "A", "text": "18 tuổi."}, {"label": "B", "text": "17 tuổi."}, {"label": "C", "text": "16 tuổi."}]'::jsonb, 'A', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (119, 1, 'Theo quy định về độ tuổi, người đủ bao nhiêu tuổi trở lên thì được cấp giấy phép lái xe mô tô hai bánh có dung tích xi lanh đến 125 cm3 và xe ô tô chở người đến 8 chỗ (không kể chỗ của người lái xe); xe ô tô tải và ô tô chuyên dùng có khối lượng toàn bộ theo thiết kế đến 3.500 kg?', '[{"label": "A", "text": "16 tuổi."}, {"label": "B", "text": "17 tuổi"}, {"label": "C", "text": "18 tuổi."}]'::jsonb, 'C', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (120, 1, 'Theo quy định về độ tuổi, người lái xe ô tô chở người (kể cả xe buýt) trên 29 chỗ (không kể chỗ của người lái xe); xe ô tô chở người giường nằm; các loại xe ô tô chở người quy định cho giấy phép lái xe hạng D kéo rơ moóc có khối lượng toàn bộ theo thiết kế đến 750 kg phải đủ bao nhiêu tuổi trở lên?', '[{"label": "A", "text": "23 tuổi."}, {"label": "B", "text": "24 tuổi."}, {"label": "C", "text": "27 tuổi."}, {"label": "D", "text": "30 tuổi."}]'::jsonb, 'C', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (121, 1, 'Tuổi tối đa của người lái xe ô tô chở người (kể cả xe buýt) trên 29 chỗ (không kể chỗ của người lái xe), xe ô tô chở người giường nằm là bao nhiêu tuổi?', '[{"label": "A", "text": "Đủ 55 tuổi đối với nam và đủ 50 tuổi đối với nữ."}, {"label": "B", "text": "Đủ 55 tuổi đối với nam và nữ."}, {"label": "C", "text": "Đủ 57 tuổi đối với nam và đủ 55 tuổi đối với nữ."}]'::jsonb, 'C', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (122, 1, 'Theo quy định về độ tuổi, người lái xe ô tô chở người (kể cả xe buýt) trên 16 chỗ (không kể chỗ của người lái xe) đến 29 chỗ (không kể chỗ của người lái xe); các loại xe ô tô chở người quy định cho giấy phép lái xe hạng D2 kéo rơ moóc có khối lượng toàn bộ theo thiết kế đến 750 kg phải đủ bao nhiêu tuổi trở lên?', '[{"label": "A", "text": "23 tuổi."}, {"label": "B", "text": "24 tuổi."}, {"label": "C", "text": "22 tuổi."}]'::jsonb, 'B', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (123, 1, 'Người đủ 16 tuổi đến dưới 18 tuổi chỉ được điều khiển các loại xe nào dưới đây?', '[{"label": "A", "text": "Xe mô tô hai bánh có dung tích xi-lanh đến 125 cm3 ."}, {"label": "B", "text": "Xe gắn máy."}, {"label": "C", "text": "Xe ô tô chở người đến 08 chỗ (không kể chỗ của người lái xe); xe ô tô tải và ô tô chuyên dùng có khối lượng toàn bộ theo thiết kế đến 3.500 kg; các loại xe ô tô quy định cho giấy phép lái xe hạng B kéo rơ moóc có khối lượng toàn bộ theo thiết kế đến 750 kg."}, {"label": "D", "text": "Cả ba ý trên."}]'::jsonb, 'B', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (124, 1, 'Người có Giấy phép lái xe mô tô hạng A1 không được phép điều khiển loại xe nào dưới đây?', '[{"label": "A", "text": "Xe mô tô hai bánh có dung tích xi-lanh 125 cm3 hoặc có công suất động cơ điện đến 11 kW."}, {"label": "B", "text": "Xe mô tô ba bánh."}, {"label": "C", "text": "Cả hai ý trên."}]'::jsonb, 'B', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (125, 1, 'Người có Giấy phép lái xe mô tô hạng A1 được cấp sau ngày 01/01/2025 được phép điều khiển loại xe nào dưới đây?', '[{"label": "A", "text": "Xe mô tô hai bánh có dung tích xi-lanh đến 125 cm3 hoặc có công suất động cơ điện đến 11 kW."}, {"label": "B", "text": "Xe mô tô ba bánh."}, {"label": "C", "text": "Cả hai ý trên."}]'::jsonb, 'A', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (126, 1, 'Người có Giấy phép lái xe mô tô hạng A được phép điều khiển loại xe nào dưới đây?', '[{"label": "A", "text": "Xe mô tô hai bánh có dung tích xi-lanh đến 125 cm3 hoặc có công suất động cơ điện đến 11 kW."}, {"label": "B", "text": "Xe mô tô hai bánh có dung tích xi-lanh trên 125 cm3 hoặc có công suất động cơ điện trên 11 kW."}, {"label": "C", "text": "Cả hai ý trên."}]'::jsonb, 'C', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (127, 1, 'Người có Giấy phép lái xe ô tô hạng B được phép điều khiển loại xe nào dưới đây?', '[{"label": "A", "text": "Xe ô tô chở người đến 08 chỗ (không kể chỗ của người lái xe)."}, {"label": "B", "text": "Xe ô tô tải và ô tô chuyên dùng có khối lượng toàn bộ theo thiết kế đến 3.500 kg."}, {"label": "C", "text": "Cả hai ý trên."}]'::jsonb, 'C', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (128, 1, 'Người có Giấy phép lái xe hạng C1 được điều khiển loại xe nào dưới đây?', '[{"label": "A", "text": "Xe ô tô tải và ô tô chuyên dùng có khối lượng toàn bộ theo thiết kế trên 7.500 kg; các loại xe ô tô tải quy định cho giấy phép lái xe hạng C1 kéo rơ moóc có khối lượng toàn bộ theo thiết kế đến 750 kg."}, {"label": "B", "text": "Xe ô tô tải và ô tô chuyên dùng có khối lượng toàn bộ theo thiết kế trên 3.500 kg đến 7.500 kg; các loại xe ô tô tải quy định cho giấy phép lái xe hạng C1 kéo rơ moóc có khối lượng toàn bộ theo thiết kế đến 750 kg."}, {"label": "C", "text": "Cả hai ý trên."}]'::jsonb, 'B', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (129, 1, 'Người có Giấy phép lái xe hạng C được điều khiển loại xe nào dưới đây?', '[{"label": "A", "text": "Xe ô tô tải và ô tô chuyên dùng có khối lượng toàn bộ theo thiết kế trên 3.500 kg đến 7.500 kg; các loại xe ô tô tải quy định cho giấy phép lái xe hạng C1 kéo rơ moóc có khối lượng toàn bộ theo thiết kế đến 750 kg."}, {"label": "B", "text": "Xe ô tô tải và ô tô chuyên dùng có khối lượng toàn bộ theo thiết kế trên 7.500 kg; các loại xe ô tô tải quy định cho giấy phép lái xe hạng C kéo rơ moóc có khối lượng toàn bộ theo thiết kế đến 750 kg."}, {"label": "C", "text": "Cả hai ý trên."}]'::jsonb, 'C', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (130, 1, 'Người có Giấy phép lái xe hạng D1 được điều khiển loại xe nào dưới đây?', '[{"label": "A", "text": "Xe ô tô chở người (kể cả xe buýt) trên 16 chỗ (không kể chỗ của người lái xe) đến 29 chỗ (không kể chỗ của người lái xe); các loại xe ô tô chở người quy định cho giấy phép lái xe hạng D1 kéo rơ moóc có khối lượng toàn bộ theo thiết kế đến 750 kg."}, {"label": "B", "text": "Xe ô tô chở người trên 08 chỗ (không kể chỗ của người lái xe) đến 16 chỗ (không kể chỗ của người lái xe); các loại xe ô tô chở người quy định cho giấy phép lái xe hạng D1 kéo rơ moóc có khối lượng toàn bộ theo thiết kế đến 750 kg."}, {"label": "C", "text": "Cả hai ý trên."}]'::jsonb, 'B', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (131, 1, 'Người có Giấy phép lái xe hạng D2 được điều khiển loại xe nào dưới đây?', '[{"label": "A", "text": "Xe ô tô chở người (kể cả xe buýt) trên 16 chỗ (không kể chỗ của người lái xe) đến 29 chỗ (không kể chỗ của người lái xe); các loại xe ô tô chở người quy định cho giấy phép lái xe hạng D2 kéo rơ moóc có khối lượng toàn bộ theo thiết kế đến 750 kg."}, {"label": "B", "text": "Xe ô tô chở người trên 08 chỗ (không kể chỗ của người lái xe) đến 16 chỗ (không kể chỗ của người lái xe); các loại xe ô tô chở người quy định cho giấy phép lái xe hạng D1 kéo rơ moóc có khối lượng toàn bộ theo thiết kế đến 750 kg."}, {"label": "C", "text": "Cả hai ý trên."}]'::jsonb, 'C', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (132, 1, 'Người có Giấy phép lái xe hạng D được điều khiển loại xe nào dưới đây?', '[{"label": "A", "text": "Xe ô tô chở người (kể cả xe buýt) trên 29 chỗ (không kể chỗ của người lái xe); xe ô tô chở người giường nằm; các loại xe ô tô chở người quy định cho giấy phép lái xe hạng D kéo rơ moóc có khối lượng toàn bộ theo thiết kế đến 750 kg."}, {"label": "B", "text": "Xe ô tô chở người (kể cả xe buýt) trên 16 chỗ (không kể chỗ của người lái xe) đến 29 chỗ (không kể chỗ của người lái xe)."}, {"label": "C", "text": "Các loại xe ô tô quy định cho giấy phép lái xe hạng C kéo rơ moóc có khối lượng toàn bộ theo thiết kế trên 750 kg; xe ô tô đầu kéo kéo sơ mi rơ moóc."}, {"label": "D", "text": "Ý 1 và ý 2."}]'::jsonb, 'D', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (133, 1, 'Người có Giấy phép lái xe hạng BE được điều khiển loại xe nào dưới đây?', '[{"label": "A", "text": "Xe ô tô chở người (kể cả xe buýt) trên 29 chỗ (không kể chỗ của người lái xe); xe ô tô chở người giường nằm."}, {"label": "B", "text": "Các loại xe ô tô quy định cho giấy phép lái xe hạng B kéo rơ moóc có khối lượng toàn bộ theo thiết kế trên 750 kg."}, {"label": "C", "text": "Xe ô tô chở người (kể cả xe buýt) trên 16 chỗ (không kể chỗ của người lái xe) đến 29 chỗ (không kể chỗ của người lái xe); các loại xe ô tô chở người quy định cho giấy phép lái xe hạng D2 kéo rơ moóc có khối lượng toàn bộ theo thiết kế đến 750 kg."}]'::jsonb, 'B', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (134, 1, 'Người có Giấy phép lái xe hạng CE được điều khiển loại xe nào dưới đây?', '[{"label": "A", "text": "Các loại xe ô tô quy định cho giấy phép lái xe hạng C kéo rơ moóc có khối lượng toàn bộ theo thiết kế trên 750 kg; xe ô tô đầu kéo kéo sơ mi rơ moóc."}, {"label": "B", "text": "Các loại xe ô tô quy định cho giấy phép lái xe hạng D1 kéo rơ moóc có khối lượng toàn bộ theo thiết kế trên 750 kg."}, {"label": "C", "text": "Cả hai ý trên."}]'::jsonb, 'A', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (135, 1, 'Người có Giấy phép lái xe hạng DE được điều khiển loại xe nào dưới đây?', '[{"label": "A", "text": "Các loại xe ô tô quy định cho giấy phép lái xe hạng D kéo rơ moóc có khối lượng toàn bộ theo thiết kế trên 750 kg; xe ô tô chở khách nối toa."}, {"label": "B", "text": "Các loại xe ô tô quy định cho giấy phép lái xe hạng C kéo rơ moóc có khối lượng toàn bộ theo thiết kế trên 750 kg; xe ô tô đầu kéo kéo sơ mi rơ moóc."}, {"label": "C", "text": "Cả hai ý trên."}]'::jsonb, 'A', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (136, 1, 'Người tập lái xe ô tô khi tham gia giao thông đường bộ phải bảo đảm các điều kiện nào dưới đây?', '[{"label": "A", "text": "Phải thực hành trên xe tập lái, trên tuyến đường tập lái và có giáo viên dạy lái bảo trợ tay lái."}, {"label": "B", "text": "Phải mang theo giấy phép xe tập lái."}, {"label": "C", "text": "Cả hai ý trên."}]'::jsonb, 'A', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (137, 1, 'Người lái xe khi tham gia giao thông đường bộ phải đảm bảo các điều kiện nào dưới đây?', '[{"label": "A", "text": "Phải đủ tuổi, sức khỏe theo quy định của pháp luật; có giấy phép lái xe đang còn điểm, còn hiệu lực phù hợp với loại xe đang điều khiển do cơ quan có thẩm quyền cấp (trừ người lái xe gắn máy)."}, {"label": "B", "text": "Phải là người đứng tên trong đăng ký xe."}, {"label": "C", "text": "Cả hai ý trên."}]'::jsonb, 'A', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (138, 1, 'Khi tham gia giao thông đường bộ, người lái xe phải mang theo các giấy tờ gì?', '[{"label": "A", "text": "Chứng nhận đăng ký xe hoặc bản sao Chứng nhận đăng ký xe có chứng thực kèm bản gốc giấy tờ xác nhận của tổ chức tín dụng, chi nhánh ngân hàng nước ngoài còn hiệu lực trong trường hợp xe đang được thế chấp tại tổ chức tín dụng, chi nhánh ngân hàng nước ngoài."}, {"label": "B", "text": "Giấy phép lái xe phù hợp với loại xe đang điều khiển; chứng nhận kiểm định an toàn kỹ thuật và bảo vệ môi trường đối với xe cơ giới theo quy định của pháp luật; chứng nhận bảo hiểm bắt buộc trách nhiệm dân sự của chủ xe cơ giới."}, {"label": "C", "text": "Trường hợp các giấy tờ nêu trên đã được tích hợp vào tài khoản định danh điện tử thì việc xuất trình, kiểm tra có thể thực hiện thông qua tài khoản định danh điện tử."}, {"label": "D", "text": "Cả ba ý trên."}]'::jsonb, 'D', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (139, 1, 'Giấy phép lái xe bị thu hồi trong các trường hợp nào sau đây?', '[{"label": "A", "text": "Người được cấp giấy phép lái xe không đủ điều kiện sức khỏe theo kết luận của cơ sở khám bệnh, chữa bệnh đối với từng hạng giấy phép lái xe."}, {"label": "B", "text": "Giấy phép lái xe được cấp sai quy định."}, {"label": "C", "text": "Giấy phép lái xe đã quá thời hạn tạm giữ hoặc hết thời hiệu thi hành quyết định xử phạt vi phạm hành chính theo quy định của pháp luật về xử lý vi phạm hành chính nếu người vi phạm không đến nhận mà không có lý do chính đáng."}, {"label": "D", "text": "Cả ba ý trên."}]'::jsonb, 'D', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (140, 1, 'Người có giấy phép lái xe chưa bị trừ hết 12 điểm, được phục hồi điểm giấy phép lái xe trong trường hợp nào sau đây?', '[{"label": "A", "text": "Không được phục hồi."}, {"label": "B", "text": "Được phục hồi đủ 12 điểm, nếu không bị trừ điểm trong thời hạn 12 tháng từ ngày bị trừ điểm gần nhất."}]'::jsonb, 'B', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (141, 1, 'Người có giấy phép lái xe đã bị trừ hết điểm phải làm gì để phục hồi điểm giấy phép lái xe?', '[{"label": "A", "text": "Không vi phạm pháp luật trật tự, an toàn giao thông đường bộ trong thời gian 12 tháng kể từ ngày bị trừ hết điểm."}, {"label": "B", "text": "Sau thời hạn ít nhất là 06 tháng kể từ ngày bị trừ hết điểm, người có phép lái xe được tham gia kiểm tra nội dung kiến thức pháp luật về trật tự, an toàn giao thông đường bộ theo quy định, có kết quả đạt yêu cầu thì được phục hồi đủ 12 điểm."}, {"label": "C", "text": "Cả hai ý trên."}]'::jsonb, 'B', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (142, 1, 'Trách nhiệm của tổ chức, cá nhân đứng tên trong giấy chứng nhận đăng ký xe khi chưa thực hiện thu hồi chứng nhận đăng ký xe, biển số xe được quy định như thế nào?', '[{"label": "A", "text": "Tiếp tục chịu trách nhiệm của chủ xe."}, {"label": "B", "text": "Không chịu trách nhiệm sau khi đã chuyển nhượng, trao đổi, tặng, cho."}]'::jsonb, 'A', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (143, 1, 'Để bảo đảm điều kiện tham gia giao thông đường bộ, loại phương tiện nào phải lắp thiết bị giám sát hành trình?', '[{"label": "A", "text": "Xe ô tô kinh doanh vận tải."}, {"label": "B", "text": "Xe ô tô chở người từ 08 chỗ trở lên (không kể chỗ của người lái xe) kinh doanh vận tải, xe ô tô đầu kéo, xe cứu thương."}, {"label": "C", "text": "Cả hai ý trên."}]'::jsonb, 'C', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (144, 1, 'Tốc độ khai thác tối đa cho phép đối với xe máy chuyên dùng, xe gắn máy và các loại xe tương tự tham gia giao thông trên đường bộ (trừ đường cao tốc) là bao nhiêu km/h?', '[{"label": "A", "text": "50 km/h."}, {"label": "B", "text": "40 km/h."}, {"label": "C", "text": "60 km/h."}]'::jsonb, 'B', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (145, 1, 'Trên đường bộ, trong khu vực đông dân cư, đường đôi hoặc đường một chiều có từ hai làn xe cơ giới trở lên, xe mô tô hai bánh, ô tô chở người đến 28 chỗ không kể chỗ của người lái xe tham gia giao thông với tốc độ khai thác tối đa cho phép là bao nhiêu?', '[{"label": "A", "text": "60 km/h."}, {"label": "B", "text": "50 km/h."}, {"label": "C", "text": "40 km/h."}]'::jsonb, 'A', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (146, 1, 'Trên đường bộ (trừ đường cao tốc) trong khu vực đông dân cư, đường hai chiều hoặc đường một chiều có một làn xe cơ giới, xe mô tô hai 30 bánh, ô tô chở người đến 28 chỗ không kể chỗ của người lái xe tham gia giao thông với tốc độ khai thác tối đa cho phép là bao nhiêu?', '[{"label": "A", "text": "60 km/h."}, {"label": "B", "text": "50 km/h."}, {"label": "C", "text": "40 km/h."}]'::jsonb, 'B', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (147, 1, 'Trên đường bộ (trừ đường cao tốc) trong khu vực đông dân cư, đường hai chiều hoặc đường một chiều có một làn xe cơ giới, loại xe nào dưới đây được tham gia giao thông với tốc độ khai thác tối đa cho phép là 50 km/h?', '[{"label": "A", "text": "Ô tô tải, ô tô chở người trên 28 chỗ không kể chỗ của người lái xe."}, {"label": "B", "text": "Xe gắn máy, xe máy chuyên dùng."}, {"label": "C", "text": "Cả hai ý trên."}]'::jsonb, 'A', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (148, 1, 'Trên đường bộ (trừ đường cao tốc) trong khu vực đông dân cư, đường đôi hoặc đường một chiều có từ hai làn cơ giới trở lên, loại xe nào dưới đây được tham gia giao thông với tốc độ khai thác tối đa là 60 km/h?', '[{"label": "A", "text": "Ô tô tải, ô tô chở người trên 28 chỗ không kể chỗ của người lái xe."}, {"label": "B", "text": "Xe gắn máy, xe máy chuyên dùng."}, {"label": "C", "text": "Cả hai ý trên."}]'::jsonb, 'A', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (149, 1, 'Trên đường bộ (trừ đường cao tốc) ngoài khu vực đông dân cư, đường đôi hoặc đường một chiều có từ hai làn xe cơ giới trở lên, loại xe nào dưới đây được tham gia giao thông với tốc độ khai thác tối đa cho phép là 90 km/h?', '[{"label": "A", "text": "Xe ô tô chở người đến 28 chỗ không kể chỗ của người lái xe (trừ xe buýt); ô tô tải có trọng tải không lớn hơn 3,5 tấn."}, {"label": "B", "text": "Xe ô tô chở người trên 28 chỗ không kể chỗ người lái xe (trừ xe buýt); ô tô tải có trọng tải trên 3,5 tấn (trừ ô tô xi téc)."}, {"label": "C", "text": "Xe buýt; ô tô đầu kéo kéo sơ mi rơ moóc (trừ ô tô đầu kéo kéo sơ mi rơ moóc xi téc); xe mô tô; ô tô chuyên dùng (trừ ô tô trộn vữa, ô tô trộn bê tông lưu động)."}]'::jsonb, 'A', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (150, 1, 'Trên đường bộ ngoài khu vực đông dân cư, đường đôi hoặc đường một chiều có từ hai làn xe cơ giới trở lên (trừ đường cao tốc) loại xe nào dưới đây được tham gia giao thông với tốc độ khai thác tối đa cho phép là 80 km/h?', '[{"label": "A", "text": "Xe ô tô chở người đến 28 chỗ không kể chỗ của người lái xe (trừ xe buýt); ô tô tải có trọng tải không lớn hơn 3,5 tấn."}, {"label": "B", "text": "Xe ô tô chở người trên 28 chỗ không kể chỗ người lái xe (trừ xe buýt); ô tô tải có trọng tải trên 3,5 tấn (trừ ô tô xi téc)."}, {"label": "C", "text": "Xe buýt; ô tô đầu kéo kéo sơ mi rơ moóc (trừ ô tô đầu kéo kéo sơ mi rơ moóc xi téc); xe mô tô; ô tô chuyên dùng (trừ ô tô trộn vữa, ô tô trộn bê tông lưu động)."}, {"label": "D", "text": "Ô tô kéo rơ moóc; ô tô kéo xe khác; ô tô trộn vữa, ô tô trộn bê tông lưu động, ô tô xi téc, ô tô đầu kéo kéo sơ mi rơ moóc xi téc, ô tô kéo theo rơ moóc xi téc)."}]'::jsonb, 'B', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (151, 1, 'Trên đường bộ ngoài khu vực đông dân cư, đường đôi hoặc đường một chiều có từ hai làn xe cơ giới trở lên (trừ đường cao tốc) loại xe nào dưới đây được tham gia giao thông với tốc độ khai thác tối đa cho phép là 70 km/h?', '[{"label": "A", "text": "Xe ô tô chở người đến 28 chỗ không kể chỗ của người lái xe (trừ xe buýt); ô tô tải có trọng tải không lớn hơn 3,5 tấn."}, {"label": "B", "text": "Xe ô tô chở người trên 28 chỗ không kể chỗ người lái xe (trừ xe buýt); ô tô tải có trọng tải trên 3,5 tấn (trừ ô tô xi téc)."}, {"label": "C", "text": "Xe buýt; ô tô đầu kéo kéo sơ mi rơ moóc (trừ ô tô đầu kéo kéo sơ mi rơ moóc xi téc); xe mô tô; ô tô chuyên dùng (trừ ô tô trộn vữa, ô tô trộn bê tông lưu động)."}, {"label": "D", "text": "Ô tô kéo rơ moóc; ô tô kéo xe khác; ô tô trộn vữa, ô tô trộn bê tông lưu động, ô tô xi téc, ô tô đầu kéo kéo sơ mi rơ moóc xi téc, ô tô kéo theo rơ moóc xi téc."}]'::jsonb, 'C', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (152, 1, 'Trên đường bộ ngoài khu vực đông dân cư, đường đôi hoặc đường một chiều có từ hai làn xe cơ giới trở lên (trừ đường cao tốc) loại xe nào dưới đây được tham gia giao thông với tốc độ khai thác tối đa cho phép là 60 km/h?', '[{"label": "A", "text": "Xe ô tô chở người đến 28 chỗ không kể chỗ của người lái xe (trừ xe buýt); ô tô tải có trọng tải không lớn hơn 3,5 tấn."}, {"label": "B", "text": "Xe ô tô chở người trên 28 chỗ không kể chỗ người lái xe (trừ xe buýt); ô tô tải có trọng tải trên 3,5 tấn (trừ ô tô xi téc)."}, {"label": "C", "text": "Xe buýt; ô tô đầu kéo kéo sơ mi rơ moóc (trừ ô tô đầu kéo kéo sơ mi rơ moóc xi téc); xe mô tô; ô tô chuyên dùng (trừ ô tô trộn vữa, ô tô trộn bê tông lưu động)."}, {"label": "D", "text": "Ô tô kéo rơ moóc; ô tô kéo xe khác; ô tô trộn vữa, ô tô trộn bê tông lưu động, ô tô xi téc, ô tô đầu kéo kéo sơ mi rơ moóc xi téc, ô tô kéo theo rơ moóc xi téc."}]'::jsonb, 'D', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (153, 1, 'Trên đường bộ ngoài khu vực đông dân cư, đường hai chiều hoặc đường một chiều có một làn xe cơ giới (trừ đường cao tốc), loại xe nào dưới đây được tham gia giao thông với tốc độ khai thác tối đa cho phép là 80 km/h?', '[{"label": "A", "text": "Xe ô tô chở người đến 28 chỗ không kể chỗ của người lái xe (trừ xe buýt); ô tô tải có trọng tải không lớn hơn 3,5 tấn."}, {"label": "B", "text": "Xe ô tô chở người trên 28 chỗ không kể chỗ người lái xe (trừ xe buýt); ô tô tải có trọng tải trên 3,5 tấn (trừ ô tô xi téc)."}, {"label": "C", "text": "Xe buýt; ô tô đầu kéo kéo sơ mi rơ moóc (trừ ô tô đầu kéo kéo sơ mi rơ moóc xi téc); xe mô tô; ô tô chuyên dùng (trừ ô tô trộn vữa, ô tô trộn bê tông lưu động)."}]'::jsonb, 'A', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (154, 1, 'Trên đường bộ ngoài khu vực đông dân cư, đường hai chiều hoặc đường một chiều có một làn xe cơ giới (trừ đường cao tốc), loại xe nào dưới đây được tham gia giao thông với tốc độ khai thác tối đa cho phép là 70 km/h?', '[{"label": "A", "text": "Xe ô tô chở người đến 28 chỗ không kể chỗ của người lái xe (trừ xe buýt); ô tô tải có trọng tải không lớn hơn 3,5 tấn."}, {"label": "B", "text": "Xe ô tô chở người trên 28 chỗ không kể chỗ người lái xe (trừ xe buýt); ô tô tải có trọng tải trên 3,5 tấn (trừ ô tô xi téc)."}, {"label": "C", "text": "Xe buýt; ô tô đầu kéo kéo sơ mi rơ moóc (trừ ô tô đầu kéo kéo sơ mi rơ moóc xi téc); xe mô tô; ô tô chuyên dùng (trừ ô tô trộn vữa, ô tô trộn bê tông lưu động)."}, {"label": "D", "text": "Ô tô kéo rơ moóc; ô tô kéo xe khác; ô tô trộn vữa, ô tô trộn bê tông lưu động, ô tô xi téc, ô tô đầu kéo kéo sơ mi rơ moóc xi téc, ô tô kéo theo rơ moóc xi téc."}]'::jsonb, 'B', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (155, 1, 'Trên đường bộ ngoài khu vực đông dân cư, đường hai chiều hoặc đường một chiều có một làn xe cơ giới (trừ đường cao tốc), loại xe nào dưới đây được tham gia giao thông với tốc độ khai thác tối đa cho phép là 60 km/h?', '[{"label": "A", "text": "Xe ô tô chở người đến 28 chỗ không kể chỗ của người lái xe (trừ xe buýt); ô tô tải có trọng tải không lớn hơn 3,5 tấn."}, {"label": "B", "text": "Xe ô tô chở người trên 28 chỗ không kể chỗ người lái xe (trừ xe buýt); ô tô tải có trọng tải trên 3,5 tấn (trừ ô tô xi téc)."}, {"label": "C", "text": "Xe buýt; ô tô đầu kéo kéo sơ mi rơ moóc (trừ ô tô đầu kéo kéo sơ mi rơ moóc xi téc); xe mô tô; ô tô chuyên dùng (trừ ô tô trộn vữa, ô tô trộn bê tông lưu động)."}, {"label": "D", "text": "Ô tô kéo rơ moóc; ô tô kéo xe khác; ô tô trộn vữa, ô tô trộn bê tông lưu động, ô tô xi téc, ô tô đầu kéo kéo sơ mi rơ moóc xi téc, ô tô kéo theo rơ moóc xi téc."}]'::jsonb, 'C', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (156, 1, 'Trên đường bộ ngoài khu vực đông dân cư, đường hai chiều hoặc đường một chiều có một làn xe cơ giới (trừ đường cao tốc), loại xe nào dưới đây được tham gia giao thông với tốc độ khai thác tối đa cho phép là 50 km/h?', '[{"label": "A", "text": "Xe ô tô chở người đến 28 chỗ không kể chỗ của người lái xe (trừ xe buýt); ô tô tải có trọng tải không lớn hơn 3,5 tấn."}, {"label": "B", "text": "Xe ô tô chở người trên 28 chỗ không kể chỗ người lái xe (trừ xe buýt); ô tô tải có trọng tải trên 3,5 tấn (trừ ô tô xi téc)."}, {"label": "C", "text": "Xe buýt; ô tô đầu kéo kéo sơ mi rơ moóc (trừ ô tô đầu kéo kéo sơ mi rơ moóc xi téc); xe mô tô; ô tô chuyên dùng (trừ ô tô trộn vữa, ô tô trộn bê tông lưu động)."}, {"label": "D", "text": "Ô tô kéo rơ moóc; ô tô kéo xe khác; ô tô trộn vữa, ô tô trộn bê tông lưu động, ô tô xi téc, ô tô đầu kéo kéo sơ mi rơ moóc xi téc, ô tô kéo theo rơ moóc xi téc."}]'::jsonb, 'D', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (157, 1, 'Khi tham gia giao thông trên đường bộ (trừ đường cao tốc), loại xe nào dưới đây được tham gia giao thông với tốc độ khai thác tối đa cho phép là 50 km/h?', '[{"label": "A", "text": "Xe máy chuyên dùng, xe gắn máy và các loại xe tương tự."}, {"label": "B", "text": "Xe chở người bốn bánh có gắn động cơ khi tham gia giao thông trong phạm vi và thời gian cho phép hoạt động."}, {"label": "C", "text": "Xe chở hàng bốn bánh có gắn động cơ khi tham gia giao thông trong phạm vi và thời gian cho phép hoạt động."}]'::jsonb, 'C', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (158, 1, 'Khi tham gia giao thông trên đoạn đường không có biển báo "Cự ly tối thiểu giữa hai xe", trong điều kiện mặt đường khô ráo, không có sương mù, mặt đường không trơn trượt, địa hình bằng phẳng, đường thẳng, tầm nhìn bảo đảm, xe cơ giới đang chạy với tốc độ từ trên 80 km/h đến 100 km/h, người lái xe phải giữ khoảng cách an toàn với xe đang chạy liền trước tối thiểu là bao nhiêu?', '[{"label": "A", "text": "35 m."}, {"label": "B", "text": "55 m."}, {"label": "C", "text": "70 m."}]'::jsonb, 'C', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (159, 1, 'Khi tham gia giao thông trên đoạn đường không có biển báo "cự ly tối thiểu giữa hai xe", trong điều kiện mặt đường khô ráo, không có sương mù, mặt đường không trơn trượt, địa hình bằng phẳng, đường thẳng, tầm nhìn bảo đảm, xe cơ giới đang chạy với tốc độ từ trên 100 km/h đến 120 km/h, người lái xe phải giữ khoảng cách an toàn với xe đang chạy liền trước tối thiểu là bao nhiêu?', '[{"label": "A", "text": "55 m."}, {"label": "B", "text": "70 m."}, {"label": "C", "text": "100 m."}]'::jsonb, 'C', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (160, 1, 'Khi tham gia giao thông trên đoạn đường không có biển báo "cự ly tối thiểu giữa hai xe", trong điều kiện mặt đường khô ráo, không có sương mù, mặt đường không trơn trượt, địa hình bằng phẳng, đường thẳng, tầm nhìn bảo đảm, xe cơ giới đang chạy với tốc độ từ trên 60 km/h đến 80 km/h, người lái xe phải giữ khoảng cách an toàn với xe đang chạy liền trước tối thiểu là bao nhiêu?', '[{"label": "A", "text": "55 m."}, {"label": "B", "text": "70 m."}, {"label": "C", "text": "100 m."}]'::jsonb, 'A', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (161, 1, 'Khi tham gia giao thông trên đoạn đường không có biển báo "cự ly tối thiểu giữa hai xe", trong điều kiện mặt đường khô ráo, không có sương mù, mặt đường không trơn trượt, địa hình bằng phẳng, đường thẳng, tầm nhìn bảo đảm, xe cơ giới đang chạy với tốc độ 60 km/h, người lái xe phải giữ khoảng cách an toàn với xe đang chạy liền trước tối thiểu là bao nhiêu?', '[{"label": "A", "text": "35 m."}, {"label": "B", "text": "55 m."}, {"label": "C", "text": "70 m."}]'::jsonb, 'A', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (162, 1, 'Khi điều khiển xe chạy với tốc độ dưới 60 km/h, để bảo đảm khoảng cách an toàn giữa hai xe, người lái xe phải điều khiển xe như thế nào?', '[{"label": "A", "text": "Chủ động giữ khoảng cách an toàn phù hợp với xe chạy liền trước xe của mình."}, {"label": "B", "text": "Bảo đảm khoảng cách an toàn tùy thuộc vào mật độ phương tiện, tình hình giao thông thực tế."}, {"label": "C", "text": "Cả hai ý trên."}]'::jsonb, 'C', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (163, 1, 'Người lái xe phải giảm tốc độ thấp hơn tốc độ tối đa cho phép đến mức cần thiết, chú ý quan sát và chuẩn bị sẵn sàng những tình huống có thể xảy ra để phòng ngừa tai nạn trong các trường hợp nào dưới đây?', '[{"label": "A", "text": "Gặp biển báo nguy hiểm và cảnh báo trên đường."}, {"label": "B", "text": "Gặp biển chỉ dẫn trên đường."}, {"label": "C", "text": "Gặp biển báo hết mọi lệnh cấm."}, {"label": "D", "text": "Gặp biển báo hết hạn chế tốc độ tối đa cho phép."}]'::jsonb, 'A', TRUE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (164, 1, 'Xe đưa đón trẻ em mầm non, học sinh được ưu tiên gì khi tham gia giao thông đường bộ?', '[{"label": "A", "text": "Tổ chức phân luồng, điều tiết giao thông, bố trí nơi dừng xe, đỗ xe tại khu vực trường học và tại các điểm trên lộ trình đưa đón trẻ em mầm non, học sinh."}, {"label": "B", "text": "Được bố trí xe dẫn đường để bảo đảm an toàn giao thông."}, {"label": "C", "text": "Cả hai ý trên."}]'::jsonb, 'A', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (165, 1, 'Người điều khiển phương tiện tham gia giao thông đường bộ phải quan sát, giảm tốc độ hoặc dừng lại để bảo đảm an toàn trong các trường hợp nào dưới đây?', '[{"label": "A", "text": "Tại nơi có vạch kẻ đường hoặc báo hiệu khác dành cho người đi bộ hoặc tại nơi mà người đi bộ, xe lăn của người khuyết tật đang qua đường."}, {"label": "B", "text": "Nơi đường bộ giao nhau cùng mức với đường bộ, đường bộ giao nhau cùng mức với đường sắt; đường hẹp, đường vòng, đường quanh co, đường đèo, dốc."}, {"label": "C", "text": "Khu vực có trường học, bệnh viện, bến xe, công trình công cộng tập trung đông người, khu vực đông dân cư, chợ, khu vực đang thi công trên đường bộ, hiện trường vụ tai nạn giao thông đường bộ."}, {"label": "D", "text": "Cả ba ý trên."}]'::jsonb, 'D', TRUE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (166, 1, 'Tại những đoạn đường không bố trí biển báo hạn chế tốc độ, không bố trí biển báo khoảng cách an toàn tối thiểu giữa hai xe, người lái xe phải thực hiện quy định nào dưới đây để bảo đảm an toàn?', '[{"label": "A", "text": "Được phép lái xe không hạn chế tốc độ và khoảng cách an toàn tối thiểu với xe phía trước cùng làn đường hoặc phần đường."}, {"label": "B", "text": "Được phép lái xe không hạn chế tốc độ và khoảng cách an toàn tối thiểu với xe phía trước cùng làn đường hoặc phần đường khi tham gia giao thông trên đường nhánh."}, {"label": "C", "text": "Chấp hành quy định về tốc độ, khoảng cách an toàn tối thiểu với xe phía trước cùng làn đường hoặc phần đường."}]'::jsonb, 'C', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (167, 1, 'Khi gặp xe buýt đang dừng đón, trả khách, người điều khiển xe mô tô phải xử lý như thế nào dưới đây?', '[{"label": "A", "text": "Tăng tốc độ để nhanh chóng vượt qua xe buýt."}, {"label": "B", "text": "Quan sát, giảm tốc độ đi qua xe buýt hoặc dừng lại để bảo đảm an toàn."}]'::jsonb, 'B', TRUE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (168, 1, 'Việc vận chuyển hành khách bằng xe ô tô phải tuân thủ các quy định nào dưới đây?', '[{"label": "A", "text": "Đón, trả hành khách đúng nơi quy định; hướng dẫn sử dụng trang thiết bị an toàn trên xe; có biện pháp giữ gìn vệ sinh trong xe; vận chuyển hành khách đúng lịch trình, lộ trình đã đăng ký, trừ trường hợp bất khả kháng; không chở hành khách trên nóc xe, trong khoang chở hành lý hoặc để hành khách đu, bám bên ngoài xe."}, {"label": "B", "text": "Đón, trả hành khách theo yêu cầu của hành khách trên xe; hướng dẫn sử dụng trang thiết bị an toàn trên xe; có biện pháp giữ gìn vệ sinh trong xe; vận chuyển hành khách đúng lịch trình, lộ trình đã đăng ký, trừ trường hợp bất khả kháng; không chở hành khách trên nóc xe, trong khoang chở hành lý hoặc để hành khách đu, bám bên ngoài xe."}]'::jsonb, 'A', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (169, 1, 'Trong hoạt động vận tải đường bộ, các hành vi nào dưới đây bị nghiêm cấm?', '[{"label": "A", "text": "Vận chuyển hàng hóa cấm lưu hành."}, {"label": "B", "text": "Vận chuyển trái phép hoặc không thực hiện đầy đủ các quy định của pháp luật về vận chuyển hàng hóa nguy hiểm."}, {"label": "C", "text": "Vận chuyển động vật hoang dã."}, {"label": "D", "text": "Cả ba ý trên."}]'::jsonb, 'D', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (170, 1, 'Trong hoạt động vận tải hành khách, những hành vi nào dưới đây bị nghiêm cấm?', '[{"label": "A", "text": "Cạnh tranh nhau nhằm tăng lợi nhuận."}, {"label": "B", "text": "Giảm giá để thu hút khách."}, {"label": "C", "text": "Đe dọa, xúc phạm, tranh giành, lôi kéo hành khách; đe doạ, cưỡng ép hành khách sử dụng dịch vụ ngoài ý muốn; chuyển tải, xuống khách hoặc các hành vi khác nhằm trốn tránh phát hiện xe chở quá tải, quá số người theo quy định của pháp luật."}, {"label": "D", "text": "Cả ba ý trên."}]'::jsonb, 'C', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (171, 1, 'Thời gian lái xe liên tục của người lái xe ô tô kinh doanh vận tải được quy định như thế nào để bảo đảm an toàn giao thông?', '[{"label": "A", "text": "Không quá 4 giờ."}, {"label": "B", "text": "Không quá 6 giờ."}, {"label": "C", "text": "Không quá 8 giờ."}, {"label": "D", "text": "Liên tục tùy thuộc vào sức khỏe và khả năng của người lái xe."}]'::jsonb, 'A', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (172, 1, 'Thời gian làm việc của người lái xe ô tô kinh doanh vận tải trong một ngày được quy định như thế nào để bảo đảm an toàn giao thông?', '[{"label": "A", "text": "Không quá 6 giờ trong một ngày."}, {"label": "B", "text": "Không quá 8 giờ trong một ngày."}, {"label": "C", "text": "Không quá 10 giờ trong một ngày."}, {"label": "D", "text": "Không hạn chế tùy thuộc vào sức khỏe và khả năng của người lái xe."}]'::jsonb, 'C', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (173, 1, 'Trước khi xe khởi hành, người lái xe, nhân viên phục vụ trên xe ô tô vận chuyển hành khách có trách nhiệm nào sau đây?', '[{"label": "A", "text": "Kiểm tra các điều kiện bảo đảm an toàn của xe."}, {"label": "B", "text": "Hướng dẫn cho hành khách về an toàn giao thông đường bộ và thoát hiểm khi gặp sự cố."}, {"label": "C", "text": "Kiểm tra nhân thân và hành lý của hành khách đi xe."}, {"label": "D", "text": "Ý 1 và ý 2."}]'::jsonb, 'D', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (174, 1, 'Xe ô tô kinh doanh vận tải chở trẻ em mầm non, học sinh phải đáp ứng các yêu cầu nào dưới đây?', '[{"label": "A", "text": "Có thiết bị ghi nhận hình ảnh trẻ em mầm non, học sinh và thiết bị có chức năng cảnh báo, chống bỏ quên trẻ em trên xe; có niên hạn sử dụng không quá 20 năm; có màu sơn theo quy định của Chính phủ."}, {"label": "B", "text": "Có dây đai an toàn phù hợp với lứa tuổi hoặc sử dụng xe có ghế ngồi phù hợp với lứa tuổi theo quy định của pháp luật."}, {"label": "C", "text": "Được cấp chứng nhận đăng kí xe và gắn biển số xe, bảo đảm an toàn kĩ thuật và bảo vệ môi trường theo quy định của pháp luật; xe ô tô chở người từ 08 chỗ trở lên (không kể chỗ của người lái xe) phải lắp thiết bị giám sát hành trình và thiết bị ghi nhận hình ảnh người lái xe."}, {"label": "D", "text": "Cả ba ý trên."}]'::jsonb, 'D', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (175, 1, 'Việc vận chuyển động vật sống khi tham gia giao thông đường bộ phải thực hiện các yêu cầu nào dưới đây?', '[{"label": "A", "text": "Người lái xe phải mang đủ giấy tờ theo quy định của pháp luật."}, {"label": "B", "text": "Phương tiện vận chuyển phải có kết cấu phù hợp với loại động vật chuyên chở."}, {"label": "C", "text": "Trong quá trình vận chuyển phải chấp hành các quy định của pháp luật về trật tự, an toàn giao thông đường bộ, vệ sinh dịch tễ, phòng dịch và bảo đảm vệ sinh môi trường."}, {"label": "D", "text": "Cả ba ý trên."}]'::jsonb, 'D', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (176, 1, 'Việc vận chuyển hàng hóa nguy hiểm khi tham gia giao thông đường bộ phải chấp hành các quy định nào dưới đây?', '[{"label": "A", "text": "Phải có giấy phép vận chuyển; trong trường hợp cần thiết, đơn vị vận chuyển hàng hoá nguy hiểm phải bố trí người áp tải để bảo đảm trật tự, an toàn giao thông đường bộ."}, {"label": "B", "text": "Phải có giấy phép vận chuyển; không phải bố trí người áp tải khi tham gia giao thông đường bộ."}]'::jsonb, 'A', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (177, 1, 'Việc lưu hành xe ô tô của người nước ngoài đăng ký tại nước ngoài có tay lái ở bên phải tham gia giao thông tại Việt Nam, xe cơ giới nước ngoài do người nước ngoài đưa vào Việt Nam du lịch được quy định như như thế nào?', '[{"label": "A", "text": "Phải chấp hành quy định của pháp luật về trật tự, an toàn giao thông đường bộ của Việt Nam. Trường hợp điều ước quốc tế mà nước Cộng hòa xã hội chủ nghĩa Việt Nam là thành viên có quy định khác thì áp dụng theo điều ước quốc tế đó."}, {"label": "B", "text": "Tham gia giao thông đúng trong phạm vi, tuyến đường, thời gian đã được cơ quan có thẩm quyền của Việt Nam cấp phép."}, {"label": "C", "text": "Xe ô tô của người nước ngoài đăng ký tại nước ngoài có tay lái ở bên phải phải đi theo đoàn và có người, phương tiện hỗ trợ, hướng dẫn giao thông."}, {"label": "D", "text": "Cả ba ý trên."}]'::jsonb, 'D', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (178, 1, 'Việc sử dụng xe mô tô, xe gắn máy, xe thô sơ để vận chuyển hành khách, hàng hóa phải thực hiện các quy định nào dưới đây để đảm bảo an toàn giao thông?', '[{"label": "A", "text": "Kiểm tra điều kiện bảo đảm an toàn của xe trước khi tham gia giao thông đường bộ; mang đủ giấy tờ theo quy định của pháp luật."}, {"label": "B", "text": "Kiểm tra việc sắp xếp hàng hóa bảo đảm an toàn; không chở quá số người, chở hành lý, hàng hoá vượt quá khối lượng cho phép hoặc vượt quá khổ giới hạn của xe."}, {"label": "C", "text": "Cả hai ý trên."}]'::jsonb, 'C', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (179, 1, 'Xe vận chuyển hàng siêu trường, siêu trọng phải thực hiện các quy định nào dưới đây để bảo đảm an toàn giao thông?', '[{"label": "A", "text": "Phải chạy với tốc độ quy định trong giấy phép và phải có báo hiệu kích thước của hàng; trường hợp cần thiết, phải có người, phương tiện hỗ trợ lái xe, cảnh báo cho người, phương tiện tham gia giao thông đường bộ khác và thực hiện các biện pháp bảo đảm an toàn giao thông khi vận chuyển hàng siêu trường, siêu trọng trên đường bộ."}, {"label": "B", "text": "Phải chạy với tốc độ quy định trong giấy phép; phải có người, phương tiện hỗ trợ lái xe, cảnh báo cho người, phương tiện tham gia giao thông đường bộ khác và thực hiện các biện pháp bảo đảm an toàn giao thông khi vận chuyển hàng siêu trường, siêu trọng trên đường dành cho giao thông công cộng."}]'::jsonb, 'A', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (180, 1, 'Xe cứu hộ giao thông đường bộ cần thực hiện yêu cầu nào sau đây để bảo đảm trật tự an toàn giao thông?', '[{"label": "A", "text": "Xe cứu hộ giao thông đường bộ phải có dấu hiệu nhận diện, niêm yết thông tin trên xe, gắn thiết bị giám sát hành trình và thiết bị ghi nhận hình ảnh người lái xe."}, {"label": "B", "text": "Phải tuân thủ quy định của pháp luật về khối lượng hàng hóa chuyên chở của xe cứu hộ và khối lượng của xe được cứu hộ ghi trên Chứng nhận kiểm định an toàn kỹ thuật và bảo vệ môi trường."}, {"label": "C", "text": "Cả hai ý trên. 39"}]'::jsonb, 'C', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (181, 2, 'Người lái xe kinh doanh vận tải khi thực hiện tốt việc rèn luyện, nâng cao trách nhiệm, đạo đức nghề nghiệp sẽ thu được kết quả như thế nào dưới đây?', '[{"label": "A", "text": "Được khách hàng, xã hội tôn trọng; được đồng nghiệp quý mến, giúp đỡ; được doanh nghiệp tin dùng và đóng góp nhiều cho xã hội; thu hút được khách hàng, góp phần quan trọng trong xây dựng thương hiệu, kinh doanh có hiệu quả cao."}, {"label": "B", "text": "Được cộng điểm vào giấy phép lái xe."}, {"label": "C", "text": "Cả hai ý trên."}]'::jsonb, 'A', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (182, 2, 'Những hành vi nào sau đây thể hiện là người có văn hóa giao thông?', '[{"label": "A", "text": "Luôn tuân thủ pháp luật về trật tự, an toàn giao thông đường bộ, nhường nhịn và giúp đỡ người khác."}, {"label": "B", "text": "Đi nhanh, vượt đèn đỏ nếu không có lực lượng Công an."}, {"label": "C", "text": "Bấm còi và nháy đèn liên tục để cảnh báo xe khác."}, {"label": "D", "text": "Tránh nhường đường để đi nhanh hơn."}]'::jsonb, 'A', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (183, 2, 'Người lái xe kinh doanh vận tải cần thực hiện những công việc gì ghi ở dưới đây để thường xuyên rèn luyện nâng cao đạo đức nghề nghiệp?', '[{"label": "A", "text": "Quản lý và sử dụng xe tốt; bảo dưỡng xe đúng định kỳ; thực hành tiết kiệm vật tư, nhiên liệu; luôn tu dưỡng bản thân, có lối sống lành mạnh, tác phong làm việc công nghiệp."}, {"label": "B", "text": "Nắm vững và tự giác chấp hành các quy định của pháp luật về trật tự, an toàn giao thông đường bộ, lái xe an toàn; thực hiện đầy đủ trách nhiệm với khách hàng; có ý thức tổ chức kỷ luật và xây dựng doanh nghiệp vững mạnh; có tinh thần hợp tác, tương trợ, giúp đỡ đồng nghiệp."}, {"label": "C", "text": "Cả hai ý trên."}]'::jsonb, 'C', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (184, 2, 'Người lái xe ô tô vận chuyển hành khách phải có những phẩm chất, đạo đức nghề nghiệp gì dưới đây?', '[{"label": "A", "text": "Phải có thái độ lịch sự, tôn trọng, thân mật với hành khách; giúp đỡ những người có hoàn cảnh khó khăn, người già, người khuyết tật, phụ nữ có thai, có con nhỏ và trẻ em."}, {"label": "B", "text": "Luôn tu dưỡng bản thân, có lối sống lành mạnh, khiêm tốn, có tác phong làm việc công nghiệp, không tham gia vào các tệ nạn xã hội; tôn trọng người cùng tham gia giao thông đường bộ và có ý thức bảo vệ môi trường."}, {"label": "C", "text": "Cả hai ý trên."}]'::jsonb, 'C', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (185, 2, 'Khái niệm về văn hóa giao thông được hiểu như thế nào là đúng?', '[{"label": "A", "text": "Là sự hiểu biết và chấp hành nghiêm chỉnh pháp luật về giao thông, là ý thức trách nhiệm với cộng đồng khi tham gia giao thông."}, {"label": "B", "text": "Là sự tôn trọng, nhường nhịn, giúp đỡ và ứng xử có văn hóa giữa những người tham gia giao thông với nhau."}, {"label": "C", "text": "Cả hai ý trên."}]'::jsonb, 'C', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (186, 2, 'Trên làn đường dành cho ô tô có vũng nước lớn, người lái xe ô tô bắt buộc phải đi qua vũng nước, trên làn đường bên cạnh có nhiều người đang lái xe mô tô tham gia giao thông, người lái xe ô tô xử lý như thế nào khi lái xe qua vũng nước là có văn hóa giao thông?', '[{"label": "A", "text": "Cho xe chạy thật nhanh qua vũng nước."}, {"label": "B", "text": "Giảm tốc độ cho xe chạy chậm qua vũng nước."}, {"label": "C", "text": "Giảm tốc độ cho xe chạy qua làn đường dành cho mô tô để tránh vũng nước."}]'::jsonb, 'B', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (187, 2, 'Người lái xe không điều khiển xe đi đúng làn đường quy định, phóng nhanh, vượt ẩu, vượt đèn đỏ, đi vào đường cấm được coi là hành vi nào trong các hành vi dưới đây?', '[{"label": "A", "text": "Là thiếu văn hóa giao thông, vi phạm pháp luật về trật tự, an toàn giao thông đường bộ."}, {"label": "B", "text": "Là thiếu văn hóa giao thông."}]'::jsonb, 'A', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (188, 2, 'Khi sơ cứu người bị tai nạn giao thông đường bộ, có vết thương chảy máu ngoài, phun thành tia và phun mạnh khi mạch đập, bạn phải làm gì dưới đây?', '[{"label": "A", "text": "Thực hiện cầm máu trực tiếp."}, {"label": "B", "text": "Thực hiện cầm máu không trực tiếp (chặn động mạch)."}]'::jsonb, 'B', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (189, 2, 'Người lái xe có văn hóa giao thông khi tham gia giao thông đường bộ phải đáp ứng các điều kiện nào dưới đây?', '[{"label": "A", "text": "Hiểu biết và chấp hành nghiêm chỉnh pháp luật về giao thông đường bộ; có ý thức trách nhiệm với cộng đồng khi tham gia giao thông; tôn trọng, nhường nhịn, giúp đỡ và ứng xử có văn hóa với những người cùng tham gia giao thông."}, {"label": "B", "text": "Điều khiển xe vượt quá tốc độ, đi không đúng làn đường."}]'::jsonb, 'A', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (190, 2, 'Người lái xe có văn hóa giao thông khi tham gia giao thông phải tuân thủ những quy định nào dưới đây?', '[{"label": "A", "text": "Điều khiển xe đi trên phần đường, làn đường có ít phương tiện tham gia giao thông, chỉ đội mũ bảo hiểm ở nơi có biển báo bắt buộc đội mũ bảo hiểm."}, {"label": "B", "text": "Chấp hành hiệu lệnh, chỉ dẫn của người điều khiển giao thông, quy định về tốc độ, tín hiệu đèn, biển báo hiệu, vạch kẻ đường khi lái xe; nhường đường cho người đi bộ, người già, trẻ em, người khuyết tật."}]'::jsonb, 'B', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (191, 2, 'Người lái xe mô tô có văn hóa giao thông khi tham gia giao thông phải tuân thủ những quy định nào dưới đây?', '[{"label": "A", "text": "Điều khiển xe đi bên phải theo chiều đi của mình; đi đúng phần đường, làn đường quy định; đội mũ bảo hiểm đúng quy chuẩn kỹ thuật quốc gia, cài quai đúng quy cách."}, {"label": "B", "text": "Điều khiển xe đi trên phần đường, làn đường có ít phương tiện tham gia giao thông."}, {"label": "C", "text": "Điều khiển xe và đội mũ bảo hiểm ở nơi có biển báo bắt buộc đội mũ bảo hiểm."}]'::jsonb, 'A', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (192, 2, 'Trong các hành vi dưới đây, người lái xe có văn hóa giao thông phải ứng xử như thế nào?', '[{"label": "A", "text": "Điều khiển xe đi bên phải theo chiều đi của mình; đi đúng phần đường, làn đường quy định; dừng, đỗ xe đúng nơi quy định; đã uống rượu, bia thì không lái xe."}, {"label": "B", "text": "Điều khiển xe đi trên phần đường, làn đường có ít phương tiện giao thông; dừng xe, đỗ xe ở nơi thuận tiện hoặc theo yêu cầu của hành khách, của người thân."}, {"label": "C", "text": "Dừng và đỗ xe ở nơi thuận tiện cho việc chuyên chở hành khách và giao nhận hàng hóa; sử dụng ít rượu, bia thì có thể lái xe."}]'::jsonb, 'A', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (193, 2, 'Khi tham gia giao thông việc sử dụng còi xe nên dùng như thế nào để thể hiện là người có văn hóa giao thông?', '[{"label": "A", "text": "Chỉ bấm còi khi thật sự cần thiết, không bấm còi liên tục hoặc kéo dài, sử dụng còi với mức âm lượng theo quy định."}, {"label": "B", "text": "Bấm còi liên tục để các xe khác nhường đường."}, {"label": "C", "text": "Bấm còi to khi đi qua khu vực đông dân cư."}, {"label": "D", "text": "Không cần dùng còi, tránh gây tiếng ồn là văn minh."}]'::jsonb, 'A', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (194, 2, 'Người điều khiển phương tiện tham giao thông đường bộ gây ra tai nạn giao thông đường bộ, người liên quan đến vụ tai nạn giao thông đường bộ có trách nhiệm gì dưới đây?', '[{"label": "A", "text": "Dừng ngay phương tiện, cảnh báo nguy hiểm, giữ nguyên hiện trường, trợ giúp người bị nạn và báo tin cho cơ quan Công an, cơ sở khám bệnh, chữa bệnh."}, {"label": "B", "text": "Ở lại hiện trường vụ tai nạn giao thông đường bộ cho đến khi người của cơ quan Công an đến, trừ trường hợp phải đi cấp cứu, đưa người bị nạn đi cấp cứu hoặc xét thấy bị đe dọa đến tính mạng, sức khỏe nhưng phải đến trình báo ngay cơ quan Công an, Ủy ban nhân dân nơi gần nhất."}, {"label": "C", "text": "Cung cấp thông tin xác định danh tính về bản thân, người liên quan đến vụ tai nạn giao thông đường bộ và thông tin liên quan của vụ tai nạn giao thông đường bộ cho cơ quan có thẩm quyền."}, {"label": "D", "text": "Cả ba ý trên."}]'::jsonb, 'D', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (195, 2, 'Người có mặt tại nơi xảy ra vụ tai giao thông đường bộ có trách nhiệm gì dưới đây?', '[{"label": "A", "text": "Giúp đỡ, cứu chữa kịp thời người bị nạn; báo tin ngay cho cơ quan Công an, cơ sở khám bệnh, chữa bệnh hoặc Ủy ban nhân dân nơi gần nhất; tham gia bảo vệ hiện trường; tham gia bảo vệ tài sản của người bị nạn; cung cấp thông tin liên quan về vụ tai nạn theo yêu cầu của cơ quan có thẩm quyền."}, {"label": "B", "text": "Chụp lại hình ảnh vụ tai nạn (nếu có thiết bị ghi hình) và nhanh chóng rời khỏi hiện trường vụ tai nạn."}]'::jsonb, 'A', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (196, 2, 'Khi sơ cứu ban đầu cho người bị tai nạn giao thông đường bộ không còn hô hấp, người lái xe và người có mặt tại hiện trường vụ tai nạn phải thực hiện các công việc gì dưới đây?', '[{"label": "A", "text": "Đặt nạn nhân nằm ngửa, khai thông đường thở của nạn nhân; thực hiện các biện pháp hô hấp nhân tạo."}, {"label": "B", "text": "Thực hiện các biện pháp hô hấp nhân tạo."}]'::jsonb, 'A', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (197, 2, 'Hành vi bỏ trốn sau khi gây tai nạn để trốn tránh trách nhiệm hoặc khi có điều kiện mà cố ý không cứu giúp người bị tai nạn giao thông có bị nghiêm cấm hay không?', '[{"label": "A", "text": "Không bị nghiêm cấm."}, {"label": "B", "text": "Nghiêm cấm tuỳ từng trường hợp cụ thể."}, {"label": "C", "text": "Bị nghiêm cấm."}]'::jsonb, 'C', TRUE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (198, 2, 'Khi đang lái xe, thấy một người đi bộ đang sang đường trên vạch kẻ đường dành cho người đi bộ, người lái xe nên làm gì?', '[{"label": "A", "text": "Giảm tốc độ và nhường đường cho người đi bộ."}, {"label": "B", "text": "Bấm còi để họ đi nhanh hơn."}, {"label": "C", "text": "Tiếp tục đi nếu đang vội."}, {"label": "D", "text": "Vượt qua nếu thấy khoảng trống đủ rộng."}]'::jsonb, 'A', TRUE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (199, 2, 'Khi gặp vụ tai nạn giao thông, người lái xe nên hành động như thế nào?', '[{"label": "A", "text": "Chạy đi để tránh rắc rối."}, {"label": "B", "text": "Dừng xe, hỗ trợ nạn nhân và báo cho cơ quan chức năng."}, {"label": "C", "text": "Chỉ quan sát rồi tiếp tục di chuyển."}, {"label": "D", "text": "Chỉ giúp đỡ nếu có người thân trong vụ tai nạn."}]'::jsonb, 'B', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (200, 2, 'Trong đoạn đường hai chiều tại khu đông dân cư đang ùn tắc, người điều khiển xe mô tô có văn hóa giao thông sẽ lựa chọn cách xử lý tình huống nào dưới đây?', '[{"label": "A", "text": "Cho xe lấn sang làn ngược chiều để nhanh chóng thoát khỏi nơi ùn tắc."}, {"label": "B", "text": "Điều khiển xe trên vỉa hè để nhanh chóng thoát khỏi nơi ùn tắc."}, {"label": "C", "text": "Kiên nhẫn tuân thủ hướng dẫn của người điều khiển giao thông hoặc tín hiệu đèn giao thông, di chuyển trên đúng phần đường bên phải theo chiều đi, nhường đường cho các phương tiện đi ngược chiều."}]'::jsonb, 'C', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (201, 2, 'Khi điều khiển phương tiện tham gia giao thông trên đoạn đường bộ đang xảy ra ùn tắc, những hành vi nào dưới đây là thiếu văn hóa?', '[{"label": "A", "text": "Bấm còi liên tục thúc giục các phương tiện phía trước nhường đường; đi trên vỉa hè, tận dụng mọi khoảng trống để nhanh chóng thoát khỏi nơi ùn tắc; lấn sang trái đường cố gắng vượt lên xe khác."}, {"label": "B", "text": "Tuân thủ hướng dẫn của người điều khiển giao thông hoặc tín hiệu giao thông, đi đúng phần đường, làn đường quy định."}]'::jsonb, 'A', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (202, 2, 'Người lái xe, chủ phương tiện giao thông cơ giới đường bộ nên thực hiện các biện pháp phòng cháy, chữa cháy như thế nào dưới đây?', '[{"label": "A", "text": "Không lắp đặt thêm thiết bị điện, phụ kiện không đúng theo thiết kế của nhà sản xuất; tuân thủ quy trình vận hành bảo trì bảo dưỡng định kỳ; thường xuyên kiểm tra phương tiện, khi phát hiện thấy dấu hiệu khác lạ (khó nổ, có hơi xăng, có tiếng kêu bất thường, nhiệt độ của máy cao, có mùi khét) cần khắc phục ngay."}, {"label": "B", "text": "Khi để xe trong nhà, ở nơi trông giữ xe phải tắt khóa điện, để xa nguồn lửa, nguồn nhiệt; không mua xăng, dầu ở các điểm bán tự phát, không rõ nguồn gốc; không sử dụng các biện pháp “tiết kiệm xăng, dầu” khi chưa được kiểm chứng về mức độ an toàn có thể gây hỏng xe hoặc dẫn đến nguy cơ xảy ra cháy cao."}, {"label": "C", "text": "Không để các chất dễ cháy, dễ bắt lửa trong xe, dưới yên xe, trong cốp xe, trong khoang động cơ. Đối với xe điện cần chọn địa điểm sạc an toàn, sử dụng các thiết bị sạc đúng cách và giám sát quá trình sạc."}, {"label": "D", "text": "Cả ba ý trên."}]'::jsonb, 'D', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (203, 2, 'Người điều khiển phương tiện cơ giới đường bộ phải thực hiện những biện pháp gì dưới đây để chữa cháy trong trường hợp phương tiện bị cháy, xét thấy có khả năng dập tắt đám cháy?', '[{"label": "A", "text": "Tắt khoá điện, hô hoán để mọi người đến trợ giúp chữa cháy, gọi Cảnh sát phòng cháy chữa cháy và cứu nạn cứu hộ (điện thoại 114)."}, {"label": "B", "text": "Nếu nhiên liệu chưa trào ra ngoài (ngọn lửa chưa cháy dữ dội) thì có thể sử dụng nước, hoặc bất kỳ chất, phương tiện chữa cháy có được để dập lửa; nếu nhiên liệu đã trào ra ngoài (ngọn lửa cháy dữ dội) thì phải sử dụng các bình chữa cháy, cát, chăn chiên, bao tải, vải nhúng nước để dập lửa."}, {"label": "C", "text": "Cả hai ý trên."}]'::jsonb, 'C', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (204, 2, 'Khi phát hiện thấy có ngọn lửa, khói hoặc nhiệt độ cao bất thường từ phương tiện giao thông do mình điều khiển người lái xe phải làm gì trước tiên?', '[{"label": "A", "text": "Bình tĩnh, đưa xe sát vào lề đường, tránh xa nơi có nhiều người, nhiều chất dễ cháy và tắt khóa điện, thực hiện các bước chữa cháy."}, {"label": "B", "text": "Hô hoán để mọi người đến trợ giúp chữa cháy, gọi Cảnh sát giao thông, Cảnh sát phòng cháy, chữa cháy và cứu nạn, cứu hộ, lực lượng y tế để sẵn sàng hỗ trợ cứu người."}, {"label": "C", "text": "Nếu nhiên liệu trào ra ngoài, ngọn lửa chưa cháy dữ dội thì tiếp tục sử dụng nước, hoặc bất kỳ chất, phương tiện chữa cháy có được để dập lửa."}, {"label": "D", "text": "Cả ba ý trên."}]'::jsonb, 'A', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (205, 2, 'Khi gặp nạn nhân bị bỏng trong vụ tai nạn giao thông, người lái xe cần làm gì?', '[{"label": "A", "text": "Gọi số 115 để thông báo về tình trạng tai nạn và yêu cầu hỗ trợ y tế ngay lập tức. Quan sát hiện trường để giúp nạn nhân, đồng thời tránh gây tổn thương cho mình."}, {"label": "B", "text": "Nhanh chóng loại bỏ nguyên nhân gây bỏng bằng cách tách nạn nhân khỏi vật gây cháy, cởi bỏ quần áo nếu bén lửa, ngâm vùng da bị bỏng vào nước sạch hoặc đắp khăn mát, lưu ý không dùng khăn hoặc nước quá lạnh."}, {"label": "C", "text": "Nếu nạn nhân còn tỉnh, cần cho uống bù nước. Trong thời tiết lạnh, cần giữ ấm cho cơ thể nạn nhân, sau đó nhanh chóng đưa đến cơ sở y tế gần nhất."}, {"label": "D", "text": "Cả ba ý trên. 45"}]'::jsonb, 'D', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (206, 3, 'Khi điều khiển xe mô tô tay ga xuống đường dốc dài, độ dốc cao, người lái xe cần thực hiện các thao tác nào dưới đây để bảo đảm an toàn?', '[{"label": "A", "text": "Giữ tay ga ở mức độ phù hợp, sử dụng phanh trước và phanh sau để giảm tốc độ."}, {"label": "B", "text": "Nhả hết tay ga, tắt động cơ, sử dụng phanh trước và phanh sau để giảm tốc độ."}, {"label": "C", "text": "Sử dụng phanh trước để giảm tốc độ kết hợp với tắt chìa khóa điện của xe."}]'::jsonb, 'A', TRUE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (207, 3, 'Khi vào số để khởi hành xe ô tô có số tự động, người lái xe phải thực hiện các thao tác nào để bảo đảm an toàn?', '[{"label": "A", "text": "Đạp bàn đạp phanh chân hết hành trình, vào số và nhả phanh đỗ, kiểm tra lại xem có bị nhầm số không rồi mới cho xe lăn bánh."}, {"label": "B", "text": "Đạp bàn đạp để tăng ga với mức độ phù hợp, vào số và kiểm tra lại xem có bị nhầm số không rồi mới cho xe lăn bánh."}]'::jsonb, 'A', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (208, 3, 'Khi nhả phanh đỗ cơ khí điều khiển bằng tay (phanh tay), người lái xe cần phải thực hiện các thao tác nào?', '[{"label": "A", "text": "Dùng lực tay phải kéo cần phanh tay về phía sau hết hành trình; nếu khóa hãm bị kẹt cứng phải đẩy mạnh phanh tay về phía trước, sau đó bóp khóa hãm."}, {"label": "B", "text": "Dùng lực tay phải bóp khóa hãm đẩy cần phanh tay về phía trước hết hành trình; nếu khóa hãm bị kẹt cứng phải kéo cần phanh tay về phía sau đồng thời bóp khóa hãm."}, {"label": "C", "text": "Dùng lực tay phải đẩy cần phanh tay về phía trước hết hành trình; nếu khóa hãm bị kẹt cứng phải đẩy mạnh phanh tay về phía trước, sau đó bóp khóa hãm."}]'::jsonb, 'B', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (209, 3, 'Khi khởi hành ô tô sử dụng hộp số cơ khí trên đường bằng, người lái xe cần thực hiện các thao tác nào theo trình tự dưới đây?', '[{"label": "A", "text": "Kiểm tra an toàn xung quanh xe ô tô; nhả từ từ đến 1/2 hành trình bàn đạp ly hợp (côn) và giữ trong khoảng 3 giây; vào số 1; nhả hết phanh tay, báo hiệu bằng còi, đèn trước khi xuất phát; tăng ga đủ để xuất phát, sau đó vừa tăng ga vừa nhả hết côn để cho xe ô tô chuyển động."}, {"label": "B", "text": "Kiểm tra an toàn xung quanh xe ô tô; đạp ly hợp (côn) hết hành trình; vào số 1; nhả hết phanh tay, báo hiệu bằng còi, đèn trước khi xuất phát; tăng ga đủ để xuất phát; nhả từ từ đến 1/2 hành trình bàn đạp côn và giữ trong khoảng 3 giây, sau đó vừa tăng ga vừa nhả hết côn để cho xe ô tô chuyển động."}]'::jsonb, 'B', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (210, 3, 'Khi quay đầu xe, người lái xe cần phải quan sát và thực hiện các thao tác nào để bảo đảm an toàn giao thông?', '[{"label": "A", "text": "Quan sát biển báo hiệu để biết nơi được phép quay đầu; quan sát kỹ địa hình nơi chọn để quay đầu; lựa chọn quỹ đạo quay đầu xe cho thích hợp; quay đầu xe 46 với tốc độ thấp; thường xuyên báo tín hiệu để người, các phương tiện xung quanh được biết; nếu quay đầu xe ở nơi nguy hiểm thì đưa đầu xe về phía nguy hiểm, đưa đuôi xe về phía an toàn."}, {"label": "B", "text": "Quan sát biển báo hiệu để biết nơi được phép quay đầu; quan sát kỹ địa hình nơi chọn để quay đầu; lựa chọn quỹ đạo quay đầu xe; quay đầu xe với tốc độ tối đa; thường xuyên báo tín hiệu để người, các phương tiện xung quanh được biết; nếu quay đầu xe ở nơi nguy hiểm thì đưa đuôi xe về phía nguy hiểm và đầu xe về phía an toàn."}]'::jsonb, 'A', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (211, 3, 'Khi ngồi lái ô tô tư thế chân, lưng, tay của người lái xe như thế nào là đúng để bảo đảm an toàn và thoải mái?', '[{"label": "A", "text": "Điều chỉnh ghế sao cho chân có thể đạp phanh và ga dễ dàng, ngồi thẳng lưng, hai tay nắm vô lăng, tay trái nắm ở vị trí 9 đến 10 giờ và tay phải nắm ở vị trí từ 2 đến 4 giờ."}, {"label": "B", "text": "Ngồi sát vô lăng để dễ quan sát hoặc ngồi tự do, không cần điều chỉnh ghế."}, {"label": "C", "text": "Ngồi ngả người về phía sau nhiều để thư giãn, chân trái để ở vị trí chân ga, hai tay nắm vô lăng ở vị trí bất kỳ."}]'::jsonb, 'A', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (212, 3, 'Khi điều khiển ô tô lên dốc cao, người lái xe cần thực hiện các thao tác nào dưới đây để bảo đảm an toàn?', '[{"label": "A", "text": "Tăng lên số cao từ chân dốc, điều chỉnh ga cho xe nhanh lên dốc; đến gần đỉnh dốc phải tăng ga để xe nhanh chóng qua dốc; về số thấp, đi sát về phía bên phải đường, có tín hiệu (còi, đèn) để báo cho người lái xe đi ngược chiều biết."}, {"label": "B", "text": "Về số thấp từ chân dốc, điều chỉnh ga cho xe từ từ lên dốc; đến gần đỉnh dốc phải đi chậm, đi sát về phía bên phải đường, có tín hiệu (còi, đèn) để báo cho người lái xe đi ngược chiều biết."}]'::jsonb, 'B', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (213, 3, 'Khi điều khiển ô tô xuống dốc dài, người lái xe cần thực hiện các thao tác nào dưới đây để bảo đảm an toàn?', '[{"label": "A", "text": "Tăng lên số cao, nhả bàn đạp ga ở mức độ phù hợp, kết hợp với phanh chân để khống chế tốc độ."}, {"label": "B", "text": "Về số thấp, nhả bàn đạp ga ở mức độ phù hợp, kết hợp với phanh chân để khống chế tốc độ."}, {"label": "C", "text": "Về số không (0), nhả bàn đạp ga ở mức độ phù hợp, kết hợp với phanh chân để khống chế tốc độ."}]'::jsonb, 'B', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (214, 3, 'Khi xuống dốc, muốn dừng xe, người lái xe cần thực hiện các thao tác nào để bảo đảm an toàn?', '[{"label": "A", "text": "Có tín hiệu rẽ phải, điều khiển xe sát vào lề đường bên phải; đạp phanh sớm và mạnh hơn lúc dừng xe trên đường bằng để xe đi với tốc độ chậm đến mức dễ dàng dừng lại được; về số 1, đạp 1/2 ly hợp (côn) cho xe đến chỗ dừng; khi xe đã dừng, về số không (N), đạp phanh chân, sử dụng phanh đỗ."}, {"label": "B", "text": "Có tín hiệu rẽ phải, điều khiển xe sát vào lề đường bên trái; đạp hết hành trình ly hợp (côn) và nhả bàn đạp ga để xe đi với tốc độ chậm đến mức dễ dàng dừng lại được tại chỗ dừng; khi xe đã dừng, đạp và giữ phanh chân."}, {"label": "C", "text": "Có tín hiệu rẽ trái, điều khiển xe sát vào lề đường bên phải; đạp phanh sớm và mạnh hơn lúc dừng xe trên đường bằng để xe đi với tốc độ chậm đến mức dễ dàng dừng lại được; về số không (N) để xe đi đến chỗ dừng, khi xe đã dừng, sử dụng phanh đỗ."}]'::jsonb, 'A', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (215, 3, 'Khi điều khiển xe trên đường vòng người lái xe cần phải làm gì để bảo đảm an toàn?', '[{"label": "A", "text": "Quan sát cẩn thận các chướng ngại vật và báo hiệu bằng coi, đèn; giảm tốc độ tới mức cần thiết, về số thấp và thực hiện quay vòng với tốc độ phù hợp với bán kính cong của đường vòng."}, {"label": "B", "text": "Quan sát cẩn thận các chướng ngại vật và báo hiệu bằng còi, đèn; tăng tốc để nhanh chóng qua đường vòng và giảm tốc độ sau khi qua đường vòng."}]'::jsonb, 'A', TRUE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (216, 3, 'Khi điều khiển xe ô tô rẽ phải, người lái xe cần thực hiện các thao tác nào để bảo đảm an toàn?', '[{"label": "A", "text": "Có tín hiệu rẽ phải; quan sát an toàn phía sau; điều khiển xe sang làn đường bên trái; giảm tốc độ và quan sát an toàn phía bên phải để điều khiển xe qua chỗ đường giao nhau."}, {"label": "B", "text": "Cách chỗ rẽ một khoảng cách an toàn có tín hiệu rẽ phải; giảm tốc độ, quan sát an toàn phía trước, sau, bên phải và điều khiển xe từ từ rẽ phải."}, {"label": "C", "text": "Cách chỗ rẽ một khoảng cách an toàn có tín hiệu rẽ phải; quan sát an toàn phía sau; điều khiển xe bám sát vào phía phải đường; tăng tốc độ và quan sát an toàn phía bên trái để điều khiển xe qua chỗ đường giao nhau."}]'::jsonb, 'B', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (217, 3, 'Khi điều khiển xe ô tô rẽ trái, người lái xe cần thực hiện các thao tác nào để bảo đảm an toàn?', '[{"label": "A", "text": "Cách chỗ rẽ một khoảng cách an toàn có tín hiệu rẽ trái; giảm tốc độ, quan sát an toàn xung quanh đặc biệt là bên trái; đổi sang làn đường bên trái và điều khiển xe từ từ rẽ trái."}, {"label": "B", "text": "Cách chỗ rẽ một khoảng cách an toàn có tín hiệu rẽ trái, tăng tốc độ để xe nhanh chóng qua chỗ đường giao nhau; có tín hiệu xin đổi làn đường; quan sát an toàn xung quanh đặc biệt là bên trái; đổi làn đường sang phải để mở rộng vòng cua."}]'::jsonb, 'A', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (218, 3, 'Khi điều khiển xe sử dụng hộp số cơ khí vượt qua rãnh lớn cắt ngang mặt đường, người lái xe cần thực hiện các thao tác nào để đảm bảo an toàn?', '[{"label": "A", "text": "Vào số một (1) và từ từ cho hai bánh xe trước xuống rãnh, tăng ga cho hai bánh xe trước vượt lên khỏi rãnh, tăng số, tăng tốc độ để bánh xe sau vượt qua rãnh."}, {"label": "B", "text": "Tăng ga, tăng số để hai bánh xe trước và bánh xe sau vượt qua khỏi rãnh và chạy bình thường."}, {"label": "C", "text": "Vào số một (1) và từ từ cho hai bánh xe trước xuống rãnh, tăng ga cho hai bánh xe trước vượt lên khỏi rãnh, tiếp tục để bánh xe sau từ từ xuống rãnh rồi tăng dần ga cho xe ô tô lên khỏi rãnh."}]'::jsonb, 'C', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (219, 3, 'Khi điều khiển xe qua đường sắt, người lái xe cần phải thực hiện các thao tác nào dưới đây để bảo đảm an toàn?', '[{"label": "A", "text": "Khi có chuông báo hoặc thanh chắn đã hạ xuống, người lái xe phải dừng xe tạm thời đúng khoảng cách an toàn, kéo phanh tay nếu đường dốc hoặc phải chờ lâu."}, {"label": "B", "text": "Khi không có chuông báo hoặc thanh chắn không hạ xuống, người lái xe cần phải quan sát nếu thấy đủ điều kiện an toàn thì về số thấp, tăng ga nhẹ và không thay đổi số trong quá trình vượt qua đường sắt để tránh động cơ chết máy cho xe cho vượt qua."}, {"label": "C", "text": "Cả hai ý trên."}]'::jsonb, 'C', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (220, 3, 'Khi điều khiển xe ô tô tự đổ, người lái xe cần chú ý những điểm gì để bảo đảm an toàn?', '[{"label": "A", "text": "Khi chạy trên đường xấu, nhiều ổ gà nên chạy chậm để thùng xe không bị lắc mạnh, không gây hiện tượng lệch \"ben\"; khi chạy vào đường vòng, cần giảm tốc độ, không lấy lái gấp và không phanh gấp."}, {"label": "B", "text": "Khi chạy trên đường quốc lộ, đường bằng phẳng không cần hạ hết thùng xe xuống."}, {"label": "C", "text": "Khi đổ hàng phải chọn vị trí có nền đường cứng và phẳng, dừng hẳn xe, kéo hết phanh đỗ; sau đó mới điều khiển cơ cấu nâng \"ben\" để đổ hàng, đổ xong hàng mới hạ thùng xuống."}, {"label": "D", "text": "Ý 1 và ý 3."}]'::jsonb, 'D', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (221, 3, 'Khi điều khiển xe tăng số, người lái xe cần chú ý những điểm gì để bảo đảm an toàn?', '[{"label": "A", "text": "Không được nhìn xuống buồng lái, cần phải tăng thứ tự từ thấp đến cao, phối hợp các động tác phải nhịp nhàng, chính xác."}, {"label": "B", "text": "Nhìn xuống buồng lái để biết chính xác vị trí các tay số, cần phải tăng thứ tự từ thấp đến cao, phối hợp các động tác phải nhịp nhàng, vù ga phải phù hợp với tốc độ."}]'::jsonb, 'A', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (222, 3, 'Khi điều khiển xe giảm số, người lái xe cần phải chú ý những điểm gì để bảo đảm an toàn?', '[{"label": "A", "text": "Nhìn xuống buồng lái để biết chính xác vị trí các tay số, cần phải giảm thứ tự từ cao xuống thấp, phối hợp các động tác phải nhịp nhàng, chính xác."}, {"label": "B", "text": "Không được nhìn xuống buồng lái, cần phải giảm thứ tự từ cao xuống thấp, phối hợp các động tác phải nhịp nhàng, chính xác, vù ga phải phù hợp với tốc độ."}]'::jsonb, 'B', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (223, 3, 'Để giảm tốc độ khi ô tô đi xuống đường dốc dài, người lái xe phải thực hiện các thao tác nào để bảo đảm an toàn?', '[{"label": "A", "text": "Nhả bàn đạp ga, đạp ly hợp (côn) hết hành trình, đạp mạnh phanh chân để giảm tốc độ."}, {"label": "B", "text": "Về số thấp phù hợp, nhả bàn đạp ga, kết hợp đạp phanh chân với mức độ phù hợp để giảm tốc độ."}, {"label": "C", "text": "Nhả bàn đạp ga, tăng lên số cao, đạp phanh chân với mức độ phù hợp để giảm tốc độ."}]'::jsonb, 'B', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (224, 3, 'Khi điều khiển ô tô qua đường ngập nước, người lái xe cần thực hiện các thao tác nào để đảm bảo an toàn?', '[{"label": "A", "text": "Tăng lên số cao, tăng ga và giảm ga liên tục để thay đổi tốc độ, giữ vững tay lái để ô tô vượt qua đoạn đường ngập nước."}, {"label": "B", "text": "Đạp ly hợp (côn) hết hành trình, tăng ga và giảm ga liên tục để thay đổi tốc độ, giữ vững tay lái để ô tô vượt qua đoạn đường ngập nước."}, {"label": "C", "text": "Quan sát, ước lượng độ ngập nước mà xe ô tô có thể vượt qua an toàn, về số thấp, giữ đều ga và giữ vững tay lái để ô tô vượt qua đoạn đường ngập nước."}]'::jsonb, 'C', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (225, 3, 'Khi điều khiển xe ô tô tới gần xe chạy ngược chiều vào ban đêm, người lái xe cần thực hiện các thao tác nào để bảo đảm an toàn?', '[{"label": "A", "text": "Chuyển từ đèn chiếu xa sang đèn chiếu gần; không nhìn thẳng vào đèn của xe chạy ngược chiều mà nhìn chếch sang phía phải theo chiều chuyển động của xe mình."}, {"label": "B", "text": "Chuyển từ đèn chiếu gần sang đèn chiếu xa; không nhìn thẳng vào đèn của xe chạy ngược chiều mà nhìn chếch sang phía phải theo chiều chuyển động của xe mình."}, {"label": "C", "text": "Chuyển từ đèn chiếu xa sang đèn chiếu gần; nhìn thẳng vào đèn của xe chạy ngược chiều để tránh xe bảo đảm an toàn."}]'::jsonb, 'A', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (226, 3, 'Khi điều khiển xe ô tô trên đường trơn cần chú ý những điểm gì để bảo đảm an toàn?', '[{"label": "A", "text": "Giữ vững tay lái cho xe đi đúng vệt bánh xe đi trước, sử dụng số thấp đi chậm, giữ đều ga, đánh lái ngoặt và phanh gấp khi cần thiết."}, {"label": "B", "text": "Giữ vững tay lái cho xe đi đúng vệt bánh xe đi trước, sử dụng số thấp đi chậm, gài cầu (nếu có), giữ đều ga, không lấy nhiều lái, không đánh lái ngoặt và phanh gấp."}]'::jsonb, 'B', TRUE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (227, 3, 'Khi động cơ xe ô tô đã khởi động, bảng đồng hồ xuất hiện ký hiệu như hình vẽ dưới đây là báo hiệu tình trạng gì của xe ô tô?', '[{"label": "A", "text": "Đang sử dụng phanh đỗ."}, {"label": "B", "text": "Nhiệt độ nước làm mát quá mức cho phép."}, {"label": "C", "text": "Cửa xe đang mở."}]'::jsonb, 'A', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (228, 3, 'Khi động cơ xe ô tô đã khởi động, bảng đồng hồ xuất hiện ký hiệu như hình vẽ dưới đây là báo hiệu tình trạng gì của xe ô tô?', '[{"label": "A", "text": "Đang sử dụng phanh đỗ."}, {"label": "B", "text": "Thiếu dầu phanh."}, {"label": "C", "text": "Nhiệt độ nước làm mát tăng quá mức cho phép."}, {"label": "D", "text": "Áp suất dầu ở mức thấp."}]'::jsonb, 'D', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (229, 3, 'Khi động cơ xe ô tô đã khởi động, bảng đồng hồ xuất hiện ký hiệu như hình vẽ dưới đây là báo hiệu tình trạng gì của xe ô tô?', '[{"label": "A", "text": "Cửa xe đóng chưa chặt hoặc có cửa xe chưa đóng."}, {"label": "B", "text": "Bộ nạp ắc quy gặp sự cố kỹ thuật."}, {"label": "C", "text": "Dầu bôi trơn bị thiếu."}, {"label": "D", "text": "Ý 2 và ý 3."}]'::jsonb, 'A', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (230, 3, 'Khi động cơ xe ô tô đã khởi động, bảng đồng hồ xuất hiện ký hiệu như hình vẽ dưới đây là báo hiệu tình trạng gì của xe ô tô?', '[{"label": "A", "text": "Thiếu dầu phanh, phanh tay đang hãm."}, {"label": "B", "text": "Hệ thống túi khí an toàn gặp sự cố."}, {"label": "C", "text": "Lái xe và người ngồi ghế trước chưa cài dây đai an toàn."}, {"label": "D", "text": "Cửa đóng chưa chặt, có cửa chưa đóng."}]'::jsonb, 'C', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (231, 3, 'Khi động cơ xe ô tô đã khởi động, bảng đồng hồ xuất hiện ký hiệu như hình vẽ dưới đây là báo hiệu tình trạng gì của xe ô tô?', '[{"label": "A", "text": "Báo hiệu thiếu dầu phanh."}, {"label": "B", "text": "Áp suất lốp không đủ."}, {"label": "C", "text": "Đang sử dụng phanh đỗ."}, {"label": "D", "text": "Sắp hết nhiên liệu."}]'::jsonb, 'D', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (232, 3, 'Trong các loại nhiên liệu dưới đây, loại nhiên liệu nào giảm thiểu ô nhiễm môi trường?', '[{"label": "A", "text": "Xăng và dầu diesel."}, {"label": "B", "text": "Xăng sinh học và khí sinh học."}, {"label": "C", "text": "Ý 1 và ý 2."}]'::jsonb, 'B', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (233, 3, 'Các biện pháp tiết kiệm nhiên liệu khi chạy xe?', '[{"label": "A", "text": "Bảo dưỡng xe theo định kỳ và có kế hoạch lộ trình trước khi xe chạy."}, {"label": "B", "text": "Kiểm tra áp suất lốp theo quy định và chạy xe với tốc độ phù hợp với tình trạng mặt đường và mật độ giao thông trên đường."}, {"label": "C", "text": "Cả hai ý trên."}]'::jsonb, 'C', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (234, 3, 'Khi đã đỗ xe ô tô sát lề đường bên phải, người lái xe phải thực hiện các thao tác nào dưới đây khi mở cửa xuống xe để đảm bảo an toàn?', '[{"label": "A", "text": "Quan sát tình hình giao thông phía trước, phía sau và phía bên mở cửa xe, mở hé cánh cửa, nếu đảm bảo an toàn thì mở cửa ở mức cần thiết để xuống xe ô tô."}, {"label": "B", "text": "Mở cánh cửa và quan sát tình hình giao thông phía trước, nếu đảm bảo an toàn thì mở cửa ở mức cần thiết để xuống xe ô tô."}, {"label": "C", "text": "Mở cánh cửa hết hành trình và nhanh chóng ra khỏi xe ô tô."}]'::jsonb, 'A', TRUE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (235, 3, 'Khi điều khiển xe ô tô qua đường sắt không có rào chắn, không có người điều khiển giao thông, người lái xe phải xử lý như thế nào?', '[{"label": "A", "text": "Giảm tốc độ, dừng lại trước vạch dừng, quan sát cả hai phía, nếu không có tàu đi tới, tăng ga nhẹ vượt qua đường sắt."}, {"label": "B", "text": "Nếu thấy có tàu đi tới thì dừng lại trước vạch dừng, chỉ khi tàu đi qua mới được đi."}, {"label": "C", "text": "Cả hai ý trên."}]'::jsonb, 'C', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (236, 3, 'Khi lái xe ô tô qua đường sắt không có rào chắn, không có người điều khiển giao thông, người lái xe thực hiện thao tác: “giảm tốc độ, dừng lại trước vạch dừng, quan sát cả hai phía, nếu không có tàu đi tới, tăng ga nhẹ vượt qua đường sắt. Nếu thấy có tàu đi tới thì dừng lại trước vạch dừng, chỉ khi tàu đi qua mới được đi” để đảm bảo an toàn là đúng hay không?', '[{"label": "A", "text": "Không đúng."}, {"label": "B", "text": "Đúng."}, {"label": "C", "text": "Không cần thiết, vì nếu nhìn thấy tàu còn cách xa, người lái xe có thể tăng số cao, tăng ga để cho xe nhanh chóng vượt qua đường sắt."}]'::jsonb, 'B', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (237, 3, 'Khi điều khiển xe ô tô có hộp số tự động đi vào đường trơn trượt, lầy lội, người lái xe phải xử lý như thế nào để đảm bảo an toàn?', '[{"label": "A", "text": "Về số thấp, kết hợp phanh chân để giảm tốc độ."}, {"label": "B", "text": "Giữ nguyên tay số D, kết hợp phanh đỗ để giảm tốc độ."}, {"label": "C", "text": "Về số N (số 0), kết hợp phanh chân để giảm tốc độ."}]'::jsonb, 'A', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (238, 3, 'Khi động cơ ô tô đã khởi động, muốn điều chỉnh ghế của người lái, người lái xe phải để cần số ở vị trí nào?', '[{"label": "A", "text": "Vị trí N hoặc vị trí P hoặc số 0."}, {"label": "B", "text": "Vị trí D hoặc số 1."}, {"label": "C", "text": "Vị trí R."}]'::jsonb, 'A', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (239, 3, 'Khi điều khiển xe ô tô có hộp số tự động, người lái xe sử dụng chân như thế nào là đúng để bảo đảm an toàn?', '[{"label": "A", "text": "Không sử dụng chân trái; chân phải điều khiển bàn đạp phanh và bàn đạp ga."}, {"label": "B", "text": "Chân trái điều khiển bàn đạp phanh, chân phải điều khiển bàn đạp ga."}, {"label": "C", "text": "Không sử dụng chân phải; chân trái điều khiển bàn đạp phanh và bàn đạp ga."}]'::jsonb, 'A', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (240, 3, 'Khi tầm nhìn bị hạn chế bởi sương mù hoặc mưa to, người lái xe phải thực hiện các thao tác nào để bảo đảm an toàn?', '[{"label": "A", "text": "Tăng tốc độ, chạy gần xe trước, nhìn đèn hậu để định hướng."}, {"label": "B", "text": "Giảm tốc độ, chạy cách xa xe trước với khoảng cách an toàn, bật đèn sương mù và đèn chiếu gần."}, {"label": "C", "text": "Tăng tốc độ, bật đèn pha vượt qua xe chạy trước."}]'::jsonb, 'B', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (241, 3, 'Khi đèn pha của xe đi ngược chiều gây chói mắt, làm giảm khả năng quan sát trên đường, người lái xe xử lý như thế nào dưới đây để bảo đảm an toàn?', '[{"label": "A", "text": "Giảm tốc độ, giữ vững tay lái, nhìn chếch sang lề đường bên phải."}, {"label": "B", "text": "Bật đèn pha chiếu xa và giữ nguyên tốc độ."}, {"label": "C", "text": "Tăng tốc độ, bật đèn pha đối diện xe phía trước."}]'::jsonb, 'A', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (242, 3, 'Để đạt được hiệu quả phanh cao nhất, người lái xe mô tô phải sử dụng các kỹ năng như thế nào dưới đây?', '[{"label": "A", "text": "Sử dụng phanh trước."}, {"label": "B", "text": "Sử dụng phanh sau."}, {"label": "C", "text": "Giảm hết ga, sử dụng đồng thời cả phanh sau và phanh trước."}]'::jsonb, 'C', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (243, 3, 'Khi lái xe ô tô trên mặt đường có nhiều "ổ gà", người lái xe phải thực hiện thao tác như thế nào để bảo đảm an toàn?', '[{"label": "A", "text": "Giảm tốc độ, về số thấp và giữ đều ga."}, {"label": "B", "text": "Tăng tốc độ cho xe lướt qua nhanh."}, {"label": "C", "text": "Tăng tốc độ, đánh lái liên tục để tránh \"ổ gà\"."}]'::jsonb, 'A', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (244, 3, 'Khi điều khiển xe ô tô gặp mưa to hoặc sương mù, người lái xe phải làm gì để bảo đảm an toàn?', '[{"label": "A", "text": "Bật đèn chiếu gần và đèn vàng (nếu có), điều khiển gạt nước, điều khiển ô tô đi với tốc độ chậm để có thể quan sát được; tìm chỗ an toàn dừng xe, bật đèn dừng khẩn cấp báo hiệu cho các xe khác biết."}, {"label": "B", "text": "Bật đèn chiếu xa và đèn vàng, điều khiển gạt nước, tăng tốc độ điều khiển ô tô qua khỏi khu vực mưa hoặc sương mù."}, {"label": "C", "text": "Tăng tốc độ, bật đèn pha vượt qua xe chạy phía trước."}]'::jsonb, 'A', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (245, 3, 'Điều khiển xe ô tô trong trời mưa, người lái xe phải xử lý như thế nào để bảo đảm an toàn?', '[{"label": "A", "text": "Giảm tốc độ, tăng cường quan sát, không nên phanh gấp, không nên tăng ga hay đánh vô lăng đột ngột, bật đèn chiếu gần, mở chế độ gạt nước ở chế độ phù hợp để đảm bảo quan sát."}, {"label": "B", "text": "Phanh gấp khi xe đi vào vũng nước và tăng ga ngay sau khi ra khỏi vũng nước."}, {"label": "C", "text": "Bật đèn chiếu xa, tăng tốc độ điều khiển ô tô qua khỏi khu vực mưa."}]'::jsonb, 'A', TRUE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (246, 3, 'Khi lùi xe, người lái xe phải xử lý như thế nào để bảo đảm an toàn giao thông?', '[{"label": "A", "text": "Quan sát bên trái, bên phải, phía sau xe, có tín hiệu cần thiết và lùi xe với tốc độ phù hợp."}, {"label": "B", "text": "Quan sát phía trước xe và lùi xe với tốc độ nhanh."}, {"label": "C", "text": "Quan sát bên trái và phía trước của xe và lùi xe với tốc độ nhanh."}]'::jsonb, 'A', TRUE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (247, 3, 'Điều khiển xe ô tô trong khu vực đông dân cư cần lưu ý điều gì dưới đây?', '[{"label": "A", "text": "Giảm tốc độ đến mức an toàn, quan sát, nhường đường cho người đi bộ, giữ khoảng cách an toàn với các xe phía trước."}, {"label": "B", "text": "Đi đúng làn đường quy định, chỉ được chuyển làn đường ở nơi cho phép, nhưng phải quan sát."}, {"label": "C", "text": "Cả hai ý trên."}]'::jsonb, 'C', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (248, 3, 'Khi điều khiển xe ô tô nhập vào đường cao tốc người lái xe cần thực hiện như thế nào dưới đây để bảo đảm an toàn giao thông?', '[{"label": "A", "text": "Quan sát, phát tín hiệu, nhường đường cho các xe đang chạy trên đường cao tốc, khi đủ điều kiện an toàn thì tăng tốc độ cho xe nhập vào làn đường cao tốc, nếu có làn đường tăng tốc thì phải cho xe chạy trên làn đường đó trước khi cho xe nhập vào làn của đường cao tốc."}, {"label": "B", "text": "Phát tín hiệu, quan sát các xe đang chạy phía trước, nếu bảo đảm các điều kiện an toàn thì tăng tốc độ cho xe nhập ngay vào làn đường cao tốc."}, {"label": "C", "text": "Phát tín hiệu và lái xe nhập vào làn đường tăng tốc, quan sát các xe phía sau đang chạy trên đường cao tốc, khi đủ điều kiện an toàn thì giảm tốc độ, từ từ cho xe nhập vào làn đường cao tốc."}]'::jsonb, 'A', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (249, 3, 'Khi điều khiển xe ô tô ra khỏi đường cao tốc người lái xe cần thực hiện như thế nào dưới đây để bảo đảm an toàn giao thông?', '[{"label": "A", "text": "Quan sát phía trước để tìm biển báo chỉ dẫn \"lối ra đường cao tốc\", kiểm tra tình trạng giao thông phía sau và bên phải, nếu bảo đảm điều kiện an toàn thì phát tín hiệu và điều khiển xe chuyển dần sang làn bên phải, nếu có làn đường giảm tốc thì phải cho xe di chuyển trên làn đường đó trước khi ra khỏi đường cao tốc."}, {"label": "B", "text": "Quan sát phía trước để tìm biển báo chỉ dẫn \"lối ra đường cao tốc\", trường hợp vượt qua \"lối ra đường cao tốc\" thì phát tín hiệu, di chuyển sang làn đường giảm tốc và lùi xe quay trở lại."}]'::jsonb, 'A', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (250, 3, 'Người lái xe được dừng xe, đỗ xe trên làn dừng khẩn cấp của đường cao tốc trong trường hợp nào dưới đây?', '[{"label": "A", "text": "Xe gặp sự cố, tai nạn hoặc trường hợp khẩn cấp không thể di chuyển bình thường."}, {"label": "B", "text": "Để nghỉ ngơi, đi vệ sinh, chụp ảnh, làm việc riêng..."}, {"label": "C", "text": "Cả hai ý trên."}]'::jsonb, 'A', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (251, 3, 'Trên đường đôi muốn vượt xe kéo rơ moóc phía trước, người lái xe phải xử lý như thế nào?', '[{"label": "A", "text": "Giữ khoảng cách an toàn, tránh đi vào vị trí điểm mù của xe kéo rơ moóc, bật xi nhan trái đồng thời kết hợp đèn, còi xin vượt."}, {"label": "B", "text": "Khi xe kéo rơ moóc đã có tín hiệu cho vượt (không có chướng ngại vật phía trước và xe đi ngược chiều), thấy đủ điều kiện an toàn thì vượt dứt khoát, không di chuyển song song bên cạnh xe kéo rơ moóc."}, {"label": "C", "text": "Khi vượt qua, phải quan sát gương chiếu hậu bên phải nếu có khoảng trống an toàn mới được cho xe chạy vào làn của mình, tắt xi nhan và tiếp tục hành trình."}, {"label": "D", "text": "Cả ba ý trên."}]'::jsonb, 'D', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (252, 3, 'Khi điều khiển xe ô tô xuống đường dốc dài, độ dốc cao, người lái xe số tự động cần thực hiện các thao tác nào dưới đây để bảo đảm an toàn?', '[{"label": "A", "text": "Nhả bàn đạp ga, về số thấp, đạp phanh chân với mức độ phù hợp để giảm tốc độ."}, {"label": "B", "text": "Nhả bàn đạp ga, về số N đạp phanh chân và kéo phanh tay để giảm tốc độ."}]'::jsonb, 'A', TRUE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (253, 3, 'Khi đi từ đường nhánh ra đường chính, người lái xe phải xử lý như thế nào là đúng?', '[{"label": "A", "text": "Quan sát, giảm tốc độ, phát tín hiệu, nhường đường cho xe trên đường chính từ bất kỳ hướng nào tới."}, {"label": "B", "text": "Nháy đèn, bấm còi để xe đi trên đường chính biết và tăng tốc độ cho xe đi ra đường chính."}, {"label": "C", "text": "Quan sát xe đang đi trên đường chính, nếu là xe có kích thước lớn hơn thì nhường đường, xe có kích thước nhỏ hơn thì tăng tốc độ cho xe đi ra đường chính."}]'::jsonb, 'A', TRUE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (254, 3, 'Khi đang lái xe mô tô hoặc ô tô, nếu có nhu cầu sử dụng điện thoại để nhắn tin hoặc gọi điện, người lái xe phải thực hiện như thế nào trong các tình huống nêu dưới đây?', '[{"label": "A", "text": "Giảm tốc độ để bảo đảm an toàn với xe phía trước và sử dụng điện thoại để liên lạc."}, {"label": "B", "text": "Giảm tốc độ để dừng xe ở nơi cho phép sau đó sử dụng điện thoại để liên lạc."}, {"label": "C", "text": "Tăng tốc độ để cách xa xe phía sau và sử dụng điện thoại để liên lạc."}]'::jsonb, 'B', TRUE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (255, 3, 'Những thói quen nào dưới đây khi điều khiển xe mô tô tay ga tham gia giao thông dễ gây tai nạn nguy hiểm?', '[{"label": "A", "text": "Sử dụng còi."}, {"label": "B", "text": "Phanh đồng thời cả phanh trước và phanh sau."}, {"label": "C", "text": "Chỉ sử dụng phanh trước."}]'::jsonb, 'C', TRUE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (256, 3, 'Người ngồi trên xe ô tô cần thực hiện những thao tác mở cửa như thế nào dưới đây để xuống xe để bảo đảm an toàn?', '[{"label": "A", "text": "Quan sát phía trước, phía sau và bên phía mở cửa để phát hiện các phương tiện đang di chuyển tới gần, khi đủ điều kiện an toàn, mở hé cửa, sau đó mở ở mức cần thiết để xuống xe."}, {"label": "B", "text": "Quan sát tình hình giao thông phía trước, không cần quan sát phía sau và bên mở cửa; mở cánh cửa hết hành trình và nhanh chóng ra khỏi xe ô tô."}]'::jsonb, 'A', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (257, 3, 'Khi điều khiển xe mô tô quay đầu, người lái xe cần thực hiện như thế nào để bảo đảm an toàn?', '[{"label": "A", "text": "Bật tín hiệu báo rẽ trước khi quay đầu, từ từ giảm tốc độ đến mức có thể dừng lại."}, {"label": "B", "text": "Chỉ quay đầu xe tại những nơi được phép quay đầu."}, {"label": "C", "text": "Quan sát an toàn các phương tiện tới từ phía trước, phía sau, hai bên đồng thời nhường đường cho xe từ bên phải và phía trước đi tới."}, {"label": "D", "text": "Cả ba ý trên."}]'::jsonb, 'D', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (258, 3, 'Tay ga trên xe mô tô hai bánh có tác dụng gì dưới đây?', '[{"label": "A", "text": "Để điều khiển xe chạy về phía trước."}, {"label": "B", "text": "Để điều tiết công suất động cơ qua đó điều khiển tốc độ của xe."}, {"label": "C", "text": "Để điều khiển xe chạy lùi."}, {"label": "D", "text": "Ý 1 và ý 2."}]'::jsonb, 'D', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (259, 3, 'Gương chiếu hậu của xe mô tô hai bánh có tác dụng gì dưới đây?', '[{"label": "A", "text": "Để quan sát an toàn phía bên trái khi chuẩn bị rẽ trái."}, {"label": "B", "text": "Để quan sát an toàn phía bên phải khi chuẩn bị rẽ phải."}, {"label": "C", "text": "Để quan sát an toàn phía sau của bên trái và bên phải trước khi chuyển hướng."}, {"label": "D", "text": "Để quan sát an toàn phía trước cả bên trái và bên phải trước khi chuyển hướng."}]'::jsonb, 'C', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (260, 3, 'Để bảo đảm an toàn khi tham gia giao thông, người lái xe mô tô hai bánh cần điều khiển tay ga như thế nào?', '[{"label": "A", "text": "Tăng ga thật mạnh, giảm ga từ từ."}, {"label": "B", "text": "Tăng ga thật mạnh, giảm ga thật nhanh."}, {"label": "C", "text": "Tăng ga từ từ, giảm ga thật nhanh."}, {"label": "D", "text": "Tăng ga từ từ, giảm ga từ từ."}]'::jsonb, 'C', TRUE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (261, 3, 'Để tránh đổ, ngã khi điều khiển xe mô tô hai bánh ở nơi đường xấu, nhỏ và hẹp, người lái xe cần xử lý như thế nào?', '[{"label": "A", "text": "Đi ở tốc độ thấp, quan sát liên tục khoảng cách từ 05 m đến 10 m phía trước để điều chỉnh sớm hướng di chuyển."}, {"label": "B", "text": "Trong quá trình di chuyển không nên dùng phanh trước tránh làm khóa bánh dẫn hướng."}, {"label": "C", "text": "Không được lắc người sang trái hoặc phải nhiều, trọng tâm cơ thể cần trùng với trọng tâm của xe."}, {"label": "D", "text": "Cả ba ý trên."}]'::jsonb, 'D', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (262, 3, 'Khi lái xe ô tô điện xuống dốc dài, đổ đèo, người lái xe cần chú ý những vấn đề gì để bảo đảm an toàn?', '[{"label": "A", "text": "Kiểm tra hệ thống an toàn, pin của xe trước khi xuất phát."}, {"label": "B", "text": "Nhả chân ga để phanh tái sinh hoạt động."}, {"label": "C", "text": "Rà phanh chân để kịp thời xử lý tình huống khẩn cấp."}, {"label": "D", "text": "Cả ba ý trên."}]'::jsonb, 'D', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (263, 3, 'Khi lái xe ô tô điện qua khu vực ngập nước, người lái xe cần phải chú ý những vấn đề gì?', '[{"label": "A", "text": "Người lái xe phải tìm hiểu khả năng chìm trong nước của xe để xác định cung đường di chuyển."}, {"label": "B", "text": "Giữ vững tay lái, đi đều ga, tránh không để xe chết máy giữa dòng, không nên dừng xe trên đường ngập nước."}, {"label": "C", "text": "Lái xe tốc độ chậm, không tạo sóng và giữ khoảng cách an toàn với các phương tiện xung quanh, nhất là người đi bộ."}, {"label": "D", "text": "Cả ba ý trên. 58"}]'::jsonb, 'D', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (264, 4, 'Phương pháp kiểm tra mức dầu bôi trơn động cơ nào dưới đây là đúng?', '[{"label": "A", "text": "Kiểm tra que thăm dầu trên các-te. Quan sát vệt dầu trên que thăm, mức dầu này phải nằm ở mức tối đa được thể hiện trên que thăm."}, {"label": "B", "text": "Rút que thăm dầu trên các-te. Quan sát vệt dầu trên que thăm, mức dầu này phải nằm ở mức tối thiểu được thể hiện trên que thăm."}, {"label": "C", "text": "Rút que thăm dầu trên các-te, lau sạch que thăm sau đó cắm vào các-te và rút ra quan sát vệt dầu trên que thăm, mức dầu phải nằm trong khoảng vạch mức tối thiểu và tối đa được thể hiện trên que thăm."}]'::jsonb, 'C', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (265, 4, 'Xe ô tô tham gia giao thông đường bộ phải bảo đảm các quy định về chất lượng, an toàn kỹ thuật và bảo vệ môi trường nào dưới đây?', '[{"label": "A", "text": "Kính chắn gió, kính cửa phải là loại kính an toàn, bảo đảm tầm nhìn cho người điều khiển; có đủ hệ thống hãm và hệ thống chuyển hướng có hiệu lực, tay lái xe ô tô ở bên trái của xe, có còi với âm lượng đúng quy chuẩn kỹ thuật."}, {"label": "B", "text": "Có đủ đèn chiếu sáng gần và xa, đèn soi biển số, đèn báo hãm, đèn tín hiệu; có đủ bộ phận giảm thanh, giảm khói, các kết cấu phải đủ độ bền và bảo đảm tính năng vận hành ổn định."}, {"label": "C", "text": "Cả hai ý trên."}]'::jsonb, 'C', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (266, 4, 'Xe mô tô và xe ô tô tham gia giao thông trên đường bộ có bắt buộc phải đủ bộ phận giảm thanh không?', '[{"label": "A", "text": "Không bắt buộc."}, {"label": "B", "text": "Bắt buộc."}, {"label": "C", "text": "Tùy từng trường hợp."}]'::jsonb, 'B', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (267, 4, 'Xe ô tô tham gia giao thông trên đường bộ phải có đủ các loại đèn nào dưới đây?', '[{"label": "A", "text": "Đèn chiếu sáng gần và xa."}, {"label": "B", "text": "Đèn soi biển số, đèn báo hãm và đèn tín hiệu."}, {"label": "C", "text": "Dàn đèn pha trên nóc xe."}, {"label": "D", "text": "Ý 1 và ý 2."}]'::jsonb, 'D', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (268, 4, 'Kính chắn gió của xe ô tô phải bảo đảm yêu cầu nào dưới đây?', '[{"label": "A", "text": "Là loại kính an toàn, kính nhiều lớp, đúng quy cách, không rạn nứt, bảo đảm hình ảnh quan sát rõ ràng, không bị méo mó."}, {"label": "B", "text": "Là loại kính trong suốt, không rạn nứt, bảo đảm tầm nhìn cho người điều khiển về phía trước mặt và hai bên."}]'::jsonb, 'A', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (269, 4, 'Bánh xe, lốp lắp cho xe ô tô phải bảo đảm an toàn kỹ thuật như thế 59 nào dưới đây?', '[{"label": "A", "text": "Bánh xe phải có kết cấu chắc chắn, lắp đặt đúng quy cách của nhà sản xuât."}, {"label": "B", "text": "Lốp phải đủ số lượng, đủ áp suất, không phồng rộp, đúng cỡ lốp của nhà sản xuất hoặc tài liệu kỹ thuật quy định; lốp trên cùng một trục của xe sử dụng trong điều kiện hoạt động bình thường phải cùng kiểu loại."}, {"label": "C", "text": "Cả hai ý trên."}]'::jsonb, 'C', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (270, 4, 'Hệ thống lái trên xe ô tô phải bảo đảm yêu cầu nào dưới đây?', '[{"label": "A", "text": "Bảo đảm cho xe chuyển hướng chính xác, điều khiển nhẹ nhàng, an toàn ở mọi vận tốc và tải trọng trong phạm vi tính năng kỹ thuật cho phép của xe."}, {"label": "B", "text": "Khi hoạt động các cơ cấu chuyển động của hệ thống lái không được va chạm với bất kỳ bộ phận nào của xe; khi quay vô lăng lái về bên phải và bên trái thì không được có sự khác biệt đáng kể về lực tác động lên vành tay lái."}, {"label": "C", "text": "Cả hai ý trên."}]'::jsonb, 'C', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (271, 4, 'Mục đích của bảo dưỡng thường xuyên đối với xe ô tô có tác dụng gì dưới đây?', '[{"label": "A", "text": "Bảo dưỡng ô tô thường xuyên làm cho ô tô luôn luôn có tính năng kỹ thuật tốt, giảm cường độ hao mòn của các chi tiết, kéo dài tuổi thọ của xe."}, {"label": "B", "text": "Ngăn ngừa và phát hiện kịp thời các hư hỏng và sai lệch kỹ thuật để khắc phục, giữ gìn được hình thức bên ngoài."}, {"label": "C", "text": "Cả hai ý trên."}]'::jsonb, 'C', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (272, 4, 'Trong các nguyên nhân nêu dưới đây, nguyên nhân nào làm động cơ diesel không nổ?', '[{"label": "A", "text": "Hết nhiên liệu, lõi lọc nhiên liệu bị tắc, lọc khí bị tắc, nhiên liệu lẫn không khí, tạp chất."}, {"label": "B", "text": "Hết nhiên liệu, lõi lọc nhiên liệu bị tắc, lọc khí bị tắc, nhiên liệu lẫn không khí, không có tia lửa điện."}, {"label": "C", "text": "Hết nhiên liệu, lõi lọc nhiên liệu bị tắc, lọc khí bị tắc, nhiên liệu lẫn không khí và nước, không có tia lửa điện."}]'::jsonb, 'A', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (273, 4, 'Ống xả lắp trên xe ô tô phải bảo đảm yêu cầu an toàn kỹ thuật nào dưới đây?', '[{"label": "A", "text": "Ống xả không được đặt ở vị trí có thể gây cháy xe hoặc ảnh hưởng đến người ngồi trên xe và gây cản trở hoạt động của hệ thống khác."}, {"label": "B", "text": "Miệng thoát khí thải của ống xả không được hướng về phía trước và không được hướng về bên phải theo chiều tiến của xe."}, {"label": "C", "text": "Cả hai ý trên."}]'::jsonb, 'C', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (274, 4, 'Dây đai an toàn lắp trên xe ô tô phải bảo đảm yêu cầu an toàn kỹ thuật nào dưới đây?', '[{"label": "A", "text": "Đủ số lượng, lắp đặt chắc chắn không bị rách, đứt, khóa cài đóng, mở nhẹ nhàng, không tự mở, không bị kẹt; kéo ra thu vào dễ dàng, cơ cấu hãm giữ chặt dây khi giật dây đột ngột."}, {"label": "B", "text": "Đủ số lượng, lắp đặt chắc chắn không bị rách, đứt, khóa cài đóng, mở nhẹ nhàng, không tự mở, không bị kẹt; kéo ra thu vào dễ dàng, cơ cấu hãm mở ra khi giật dây đột ngột."}, {"label": "C", "text": "Cả hai ý trên."}]'::jsonb, 'A', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (275, 4, 'Thế nào là động cơ 4 kỳ?', '[{"label": "A", "text": "Là loại động cơ: để hoàn thành một chu trình công tác của động cơ, pít tông thực hiện 2 (hai) hành trình, trong đó có một lần sinh công."}, {"label": "B", "text": "Là loại động cơ: để hoàn thành một chu trình công tác của động cơ, pít tông thực hiện 4 (bốn) hành trình, trong đó có một lần sinh công."}]'::jsonb, 'B', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (276, 4, 'Công dụng của hệ thống bôi trơn đối với động cơ ô tô?', '[{"label": "A", "text": "Cung cấp một lượng dầu bôi trơn đủ và sạch dưới áp suất nhất định đi bôi trơn cho các chi tiết của động cơ để giảm ma sát, giảm mài mòn, làm kín, làm sạch, làm mát và chống gỉ."}, {"label": "B", "text": "Cung cấp một lượng nhiên liệu đầy đủ và sạch để cho động cơ ô tô hoạt động."}, {"label": "C", "text": "Cả hai ý trên."}]'::jsonb, 'A', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (277, 4, 'Niên hạn sử dụng của xe ô tô tải (tính bắt đầu từ năm sản xuất) không quá bao nhiêu năm?', '[{"label": "A", "text": "15 năm."}, {"label": "B", "text": "20 năm."}, {"label": "C", "text": "25 năm."}]'::jsonb, 'C', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (278, 4, 'Niên hạn sử dụng của xe ô tô chở người trên 8 chỗ ngồi (không kể chỗ của người lái xe) tính bắt đầu từ năm sản xuất không quá bao nhiêu năm?', '[{"label": "A", "text": "5 năm."}, {"label": "B", "text": "20 năm."}, {"label": "C", "text": "25 năm."}]'::jsonb, 'B', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (279, 4, 'Hãy nêu công dụng của động cơ xe ô tô?', '[{"label": "A", "text": "Khi làm việc, nhiệt năng được biến đổi thành cơ năng làm trục khủy động cơ quay, truyền lực đến các bánh xe chủ động tạo ra chuyển động tịnh tiến cho xe ô tô."}, {"label": "B", "text": "Khi làm việc, cơ năng được biến đổi thành nhiệt năng và truyền đến các bánh xe chủ động tạo ra chuyển động tịnh tiến cho xe ô tô."}, {"label": "C", "text": "Cả hai ý trên."}]'::jsonb, 'A', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (280, 4, 'Hãy nêu công dụng hệ thống truyền lực của xe ô tô?', '[{"label": "A", "text": "Dùng để truyền mô men quay từ động cơ tới các bánh xe chủ động của xe ô tô."}, {"label": "B", "text": "Dùng để thay đổi hướng chuyển động hoặc giữ cho xe ô tô chuyển động ổn định theo hướng xác định."}, {"label": "C", "text": "Dùng để làm giảm tốc độ, dừng chuyển động của xe ô tô."}]'::jsonb, 'A', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (281, 4, 'Hãy nêu công dụng ly hợp (côn) của xe ô tô?', '[{"label": "A", "text": "Dùng để truyền mô men xoắn giữa các trục không cùng nằm trên một đường thẳng và góc lệch trục luôn thay đổi trong quá trình xe ô tô chuyển động."}, {"label": "B", "text": "Dùng để truyền hoặc ngắt truyền động từ động cơ đến hộp số của xe ô tô."}, {"label": "C", "text": "Dùng để truyền truyền động từ hộp số đến bánh xe chủ động của ô tô."}]'::jsonb, 'B', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (282, 4, 'Hãy nêu công dụng hộp số của xe ô tô?', '[{"label": "A", "text": "Truyền và tăng mô men xoắn giữa các trục vuông góc nhau, bảo đảm cho các bánh xe chủ động quay với tốc độ khác nhau khi sức cản chuyển động ở bánh xe hai bên không bằng nhau."}, {"label": "B", "text": "Truyền và thay đổi mô men xoắn giữa các trục không cùng nằm trên một đường thẳng và góc lệch trục luôn thay đổi trong quá trình ô tô chuyển động, chuyển số êm dịu, dễ điều khiển."}, {"label": "C", "text": "Truyền và thay đổi mô men từ động cơ đến bánh xe chủ động, cắt truyền động từ động cơ đến bánh xe chủ động, bảo đảm cho xe ô tô chuyển động lùi."}]'::jsonb, 'C', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (283, 4, 'Hãy nêu công dụng hệ thống lái của xe ô tô?', '[{"label": "A", "text": "Dùng để thay đổi mô men từ động cơ tới các bánh xe chủ động khi xe ô tô chuyển động theo hướng xác định."}, {"label": "B", "text": "Dùng để thay đổi mô men giữa các trục vuông góc nhau khi xe ô tô chuyển động theo hướng xác định."}, {"label": "C", "text": "Dùng để thay đổi hướng chuyển động hoặc giữ cho xe ô tô chuyển động ổn định theo hướng xác định."}]'::jsonb, 'C', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (284, 4, 'Hãy nêu công dụng hệ thống phanh của xe ô tô?', '[{"label": "A", "text": "Dùng để giảm tốc độ, dừng chuyển động của xe ô tô và giữ cho xe ô tô đứng yên trên dốc."}, {"label": "B", "text": "Dùng để thay đổi hướng chuyển động hoặc giữ cho xe ô tô chuyển động ổn định theo hướng xác định."}, {"label": "C", "text": "Dùng để truyền hoặc ngắt truyền động từ động cơ đến bánh xe chủ động của xe ô tô."}]'::jsonb, 'A', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (285, 4, 'Đèn phanh trên xe mô tô có tác dụng gì dưới đây?', '[{"label": "A", "text": "Có tác dụng cảnh báo cho các xe phía sau biết xe đang giảm tốc để chủ động tránh hoặc giảm tốc để tránh va chạm."}, {"label": "B", "text": "Có tác dụng định vị vào ban đêm với các xe từ phía sau tới để tránh va chạm."}, {"label": "C", "text": "Cả hai ý trên."}]'::jsonb, 'C', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (286, 4, 'Khi động cơ ô tô đã khởi động, bảng đồng hồ xuất hiện ký hiệu như hình vẽ dưới đây, báo hiệu tình trạng như thế nào của xe ô tô?', '[{"label": "A", "text": "Nhiệt độ nước làm mát động cơ quá ngưỡng cho phép."}, {"label": "B", "text": "Áp suất lốp không đủ."}, {"label": "C", "text": "Đang hãm phanh tay."}, {"label": "D", "text": "Cần kiểm tra động cơ."}]'::jsonb, 'A', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (287, 4, 'Khi động cơ ô tô đã khởi động, bảng đồng hồ xuất hiện ký hiệu như hình vẽ dưới đây, báo hiệu tình trạng như thế nào của xe ô tô?', '[{"label": "A", "text": "Báo hiệu hệ thống chống bó cứng khi phanh bị lỗi."}, {"label": "B", "text": "Áp suất lốp không đủ."}, {"label": "C", "text": "Đang hãm phanh tay."}, {"label": "D", "text": "Cần kiểm tra động cơ."}]'::jsonb, 'D', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (288, 4, 'Khi động cơ ô tô đã khởi động, bảng đồng hồ xuất hiện ký hiệu như hình vẽ dưới đây, báo hiệu tình trạng như thế nào của xe ô tô?', '[{"label": "A", "text": "Báo hiệu thiếu dầu phanh."}, {"label": "B", "text": "Áp suất lốp không đủ."}, {"label": "C", "text": "Đang hãm phanh tay."}, {"label": "D", "text": "Sắp hết nhiên liệu."}]'::jsonb, 'B', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (289, 4, 'Khi động cơ ô tô đã khởi động, bảng đồng hồ xuất hiện ký hiệu như hình vẽ dưới đây, báo hiệu tình trạng như thế nào của xe ô tô?', '[{"label": "A", "text": "Báo hiệu hệ thống chống bó cứng phanh bị lỗi."}, {"label": "B", "text": "Áp suất lốp không đủ."}, {"label": "C", "text": "Đang hãm phanh tay."}, {"label": "D", "text": "Sắp hết nhiên liệu."}]'::jsonb, 'A', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (290, 4, 'Khi khởi động xe ô tô số tự động có trang bị chìa khóa thông minh có cần đạp hết hành trình bàn đạp chân phanh hay không?', '[{"label": "A", "text": "Phải đạp hết hành trình bàn đạp chân phanh."}, {"label": "B", "text": "Không cần đạp phanh."}, {"label": "C", "text": "Tùy từng trường hợp."}]'::jsonb, 'A', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (291, 4, 'Ắc quy được trang bị trên xe ô tô có tác dụng gì dưới đây?', '[{"label": "A", "text": "Giúp người lái xe kịp thời tạo xung lực tối đa lên hệ thống phanh trong khoảnh khắc đầu tiên của tình huống khẩn cấp."}, {"label": "B", "text": "Ổn định chuyển động của xe ô tô khi đi vào đường vòng."}, {"label": "C", "text": "Hỗ trợ người lái xe khởi hành ngang dốc."}, {"label": "D", "text": "Để tích trữ điện năng, cung cấp cho các phụ tải khi máy phát chưa làm việc."}]'::jsonb, 'D', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (292, 4, 'Máy phát điện được trang bị trên xe ô tô có tác dụng gì dưới đây?', '[{"label": "A", "text": "Để phát điện năng cung cấp cho các phụ tải làm việc và nạp điện cho ắc quy."}, {"label": "B", "text": "Ổn định chuyển động của xe ô tô khi đi vào đường vòng."}, {"label": "C", "text": "Hỗ trợ người lái xe khởi hành ngang dốc."}, {"label": "D", "text": "Để tích trữ điện năng và cung cấp điện cho các phụ tải làm việc."}]'::jsonb, 'A', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (293, 4, 'Dây đai an toàn được trang bị trên xe ô tô có tác dụng gì dưới đây?', '[{"label": "A", "text": "Ổn định chuyển động của xe ô tô khi đi vào đường vòng."}, {"label": "B", "text": "Giữ chặt người lái và hành khách trên ghế ngồi khi xe ô tô đột ngột dừng lại."}]'::jsonb, 'B', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (294, 4, 'Túi khí được trang bị trên xe ô tô có tác dụng gì dưới đây?', '[{"label": "A", "text": "Giữ chặt người lái và hành khách trên ghế ngồi khi xe ô tô đột ngột dừng lại."}, {"label": "B", "text": "Giảm khả năng va đập của một số bộ phận cơ thể quan trọng với các vật thể trong xe."}, {"label": "C", "text": "Hấp thụ một phần lực va đập lên người lái và hành khách."}, {"label": "D", "text": "Ý 2 và ý 3."}]'::jsonb, 'D', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (295, 4, 'Khi động cơ ô tô đã khởi động, bảng đồng hồ xuất hiện ký hiệu như hình vẽ dưới đây không tắt trong thời gian dài, báo hiệu tình trạng như thế nào của xe ô tô?', '[{"label": "A", "text": "Nhiệt độ nước làm mát động cơ quá ngưỡng cho phép."}, {"label": "B", "text": "Áp suất lốp không đủ."}, {"label": "C", "text": "Đang hãm phanh tay."}, {"label": "D", "text": "Hệ thống lái gặp sự cố."}]'::jsonb, 'D', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (296, 4, 'Trên xe ô tô có trang bị thiết bị như hình vẽ dưới đây có tác dụng gì?', '[{"label": "A", "text": "Dùng để kích (hay nâng) xe ô tô."}, {"label": "B", "text": "Vặn ốc lắp bánh xe."}, {"label": "C", "text": "Ổn định chuyển động của xe ô tô khi đi vào đường vòng."}, {"label": "D", "text": "Giữ chặt người lái và hành khách trên ghế ngồi khi xe ô tô đột ngột dừng lại."}]'::jsonb, 'A', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (297, 4, 'Trên xe ô tô có trang bị thiết bị như hình vẽ dưới đây, dùng để làm gì?', '[{"label": "A", "text": "Thay lốp xe."}, {"label": "B", "text": "Chữa cháy."}, {"label": "C", "text": "Phá cửa kính xe ô tô trong các trường hợp khẩn cấp."}, {"label": "D", "text": "Vặn ốc để tháo lắp bánh xe."}]'::jsonb, 'C', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (298, 4, 'Trên xe ô tô có trang bị thiết bị như hình vẽ dưới đây, dùng để làm gì?', '[{"label": "A", "text": "Thay lốp xe."}, {"label": "B", "text": "Chữa cháy trong các trường hợp hỏa hoạn."}, {"label": "C", "text": "Cầm máu cho người bị nạn."}]'::jsonb, 'B', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (299, 4, 'Nút bấm biểu tượng như hình vẽ trên xe ô tô có ý nghĩa như thế nào?', '[{"label": "A", "text": "Báo hiệu xin đường cho xe đi thẳng."}, {"label": "B", "text": "Báo hiệu hệ thống phanh bị lỗi."}, {"label": "C", "text": "Báo hiệu xe đang ở tình huống nguy hiểm."}, {"label": "D", "text": "Báo hiệu đến thời gian cần bảo dưỡng."}]'::jsonb, 'C', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (300, 4, 'Khi động cơ đã hoạt động, bảng đồng hồ xuất hiện biểu tượng hình vẽ dưới đây, báo hiệu tình trạng như thế nào của xe?', '[{"label": "A", "text": "Báo hiệu hệ thống túi khí đã bật."}, {"label": "B", "text": "Báo hiệu đèn chiếu sáng xa đã bật."}, {"label": "C", "text": "Báo hiệu chế độ lái tiết kiệm nhiên liệu đã bật."}, {"label": "D", "text": "Báo hiệu đèn cảnh báo khoảng cách đã bật. 67"}]'::jsonb, 'C', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (301, 5, 'Biển nào cấm các loại xe cơ giới đi vào, trừ xe máy hai bánh, xe gắn máy và các loại xe ưu tiên theo quy định?', '[{"label": "A", "text": "Biển 1. 3. Biển 1 và biển 3."}, {"label": "B", "text": "Biển 2. 4. Cả ba biển."}]'::jsonb, 'A', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (302, 5, 'Biển nào cấm xe ô tô tải?', '[{"label": "A", "text": "Cả ba biển. 3. Biển 1 và biển 3."}, {"label": "B", "text": "Biển 2 và biển 3. 4. Biển 1 và biển 2."}]'::jsonb, 'B', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (303, 5, 'Biển nào cấm máy kéo?', '[{"label": "A", "text": "Biển 1."}, {"label": "B", "text": "Biển 2 và biển 3."}, {"label": "C", "text": "Biển 1 và biển 3."}, {"label": "D", "text": "Cả ba biển."}]'::jsonb, 'B', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (304, 5, 'Biển nào báo hiệu cấm xe mô tô ba bánh chở hàng đi qua?', '[{"label": "A", "text": "Biển 1 và biển 2."}, {"label": "B", "text": "Biển 1 và biển 3."}, {"label": "C", "text": "Biển 2 và biển 3."}]'::jsonb, 'A', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (305, 5, 'Biển nào dưới đây xe gắn máy được phép đi vào?', '[{"label": "A", "text": "Biển 1."}, {"label": "B", "text": "Biển 2."}, {"label": "C", "text": "Cả hai biển."}]'::jsonb, 'C', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (306, 5, 'Biển nào báo hiệu cấm xe mô tô đi vào?', '[{"label": "A", "text": "Biển 1."}, {"label": "B", "text": "Biển 2."}, {"label": "C", "text": "Biển 3."}]'::jsonb, 'A', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (307, 5, 'Khi gặp biển nào thì xe mô tô hai bánh được đi vào?', '[{"label": "A", "text": "Không biển nào."}, {"label": "B", "text": "Biển 1 và biển 2."}, {"label": "C", "text": "Biển 2 và biển 3."}, {"label": "D", "text": "Cả ba biển."}]'::jsonb, 'C', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (308, 5, 'Biển nào cho phép xe ô tô con được vượt?', '[{"label": "A", "text": "Biển 1 và biển 2."}, {"label": "B", "text": "Biển 2."}, {"label": "C", "text": "Biển 1 và biển 3."}, {"label": "D", "text": "Biển 2 và biển 3."}]'::jsonb, 'C', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (309, 5, 'Biển nào không cho phép xe ô tô con vượt?', '[{"label": "A", "text": "Biển 1."}, {"label": "B", "text": "Biển 2."}, {"label": "C", "text": "Biển 3."}]'::jsonb, 'B', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (310, 5, 'Biển nào cấm xe ô tô tải vượt?', '[{"label": "A", "text": "Biển 1."}, {"label": "B", "text": "Biển 1 và biển 2."}, {"label": "C", "text": "Biển 1 và biển 3."}, {"label": "D", "text": "Biển 2 và biển 3."}]'::jsonb, 'D', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (311, 5, 'Biển nào cấm xe tải vượt?', '[{"label": "A", "text": "Biển 1."}, {"label": "B", "text": "Biển 2."}, {"label": "C", "text": "Cả hai biển."}]'::jsonb, 'C', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (312, 5, 'Biển nào xe ô tô con được phép vượt?', '[{"label": "A", "text": "Biển 1."}, {"label": "B", "text": "Biển 2."}]'::jsonb, 'B', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (313, 5, 'Biển nào cấm quay đầu xe?', '[{"label": "A", "text": "Biển 1."}, {"label": "B", "text": "Biển 2."}, {"label": "C", "text": "Không biển nào."}, {"label": "D", "text": "Cả hai biển."}]'::jsonb, 'B', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (314, 5, 'Biển nào cấm xe rẽ trái?', '[{"label": "A", "text": "Biển 1."}, {"label": "B", "text": "Biển 2."}, {"label": "C", "text": "Cả hai biển."}]'::jsonb, 'A', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (315, 5, 'Khi gặp biển nào xe được rẽ trái?', '[{"label": "A", "text": "Biển 1."}, {"label": "B", "text": "Biển 2."}, {"label": "C", "text": "Không biển nào."}]'::jsonb, 'B', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (316, 5, 'Biển nào cấm xe ô tô rẽ trái?', '[{"label": "A", "text": "Biển 1."}, {"label": "B", "text": "Biển 2."}, {"label": "C", "text": "Cả hai biển."}]'::jsonb, 'C', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (317, 5, 'Biển nào cấm các phương tiện rẽ phải?', '[{"label": "A", "text": "Biển 1 và biển 2."}, {"label": "B", "text": "Biển 1 và biển 3."}, {"label": "C", "text": "Biển 2 và biển 3."}, {"label": "D", "text": "Cả ba biển."}]'::jsonb, 'A', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (318, 5, 'Biển nào cấm các phương tiện rẽ trái?', '[{"label": "A", "text": "Biển 1 và biển 2."}, {"label": "B", "text": "Biển 1 và biển 3."}, {"label": "C", "text": "Biển 2 và biển 3."}, {"label": "D", "text": "Cả ba biển."}]'::jsonb, 'A', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (319, 5, 'Biển nào dưới đây chỉ cấm xe ô tô quay đầu?', '[{"label": "A", "text": "Biển 1."}, {"label": "B", "text": "Biển 2."}, {"label": "C", "text": "Biển 3."}, {"label": "D", "text": "Biển 1 và biển 3."}]'::jsonb, 'A', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (320, 5, 'Biển nào dưới đây cấm xe ô tô rẽ trái và quay đầu?', '[{"label": "A", "text": "Biển 1."}, {"label": "B", "text": "Biển 2."}, {"label": "C", "text": "Biển 3."}, {"label": "D", "text": "Biển 1 và biển 3."}]'::jsonb, 'B', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (321, 5, 'Biển nào cấm xe taxi mà không cấm các phương tiện khác?', '[{"label": "A", "text": "Biển 1."}, {"label": "B", "text": "Biển 2."}, {"label": "C", "text": "Không biển nào."}]'::jsonb, 'B', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (322, 5, 'Biển nào xe được phép rẽ trái?', '[{"label": "A", "text": "Biển 1."}, {"label": "B", "text": "Biển 2."}, {"label": "C", "text": "Không biển nào."}]'::jsonb, 'B', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (323, 5, 'Biển nào xe quay đầu không bị cấm?', '[{"label": "A", "text": "Biển 1."}, {"label": "B", "text": "Biển 2."}, {"label": "C", "text": "Cả hai biển."}]'::jsonb, 'C', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (324, 5, 'Biển nào xe được phép quay đầu nhưng không được rẽ trái?', '[{"label": "A", "text": "Biển 1."}, {"label": "B", "text": "Biển 2."}, {"label": "C", "text": "Cả hai biển."}]'::jsonb, 'A', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (325, 5, 'Biển nào là biển "Cấm đi ngược chiều"?', '[{"label": "A", "text": "Biển 1."}, {"label": "B", "text": "Biển 2."}, {"label": "C", "text": "Cả ba biển."}]'::jsonb, 'B', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (326, 5, 'Biển nào dưới đây các phương tiện không được phép đi vào?', '[{"label": "A", "text": "Biển 1."}, {"label": "B", "text": "Biển 2."}, {"label": "C", "text": "Biển 1 và biển 2."}]'::jsonb, 'C', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (327, 5, 'Gặp biển nào người lái xe không được đỗ xe vào ngày chẵn?', '[{"label": "A", "text": "Biển 1."}, {"label": "B", "text": "Biển 1 và biển 3."}, {"label": "C", "text": "Biển 2 và biển 3."}, {"label": "D", "text": "Biển 3."}]'::jsonb, 'B', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (328, 5, 'Gặp biển nào người lái xe không được đỗ xe vào ngày lẻ?', '[{"label": "A", "text": "Biển 1 và biển 2."}, {"label": "B", "text": "Biển 2."}, {"label": "C", "text": "Biển 2 và biển 3."}, {"label": "D", "text": "Biển 3."}]'::jsonb, 'A', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (329, 5, 'Khi gặp biển nào xe ưu tiên theo luật định vẫn phải dừng lại?', '[{"label": "A", "text": "Biển 1."}, {"label": "B", "text": "Biển 2."}, {"label": "C", "text": "Cả ba biển."}]'::jsonb, 'B', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (330, 5, 'Biển nào cấm tất cả các loại xe cơ giới và thô sơ đi lại trên đường, trừ xe ưu tiên theo luật định (nếu đường vẫn cho xe chạy được)?', '[{"label": "A", "text": "Biển 1."}, {"label": "B", "text": "Biển 2."}, {"label": "C", "text": "Cả hai biển."}]'::jsonb, 'A', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (331, 5, 'Biển nào là biển "Cấm xe chở hàng nguy hiểm"?', '[{"label": "A", "text": "Biển 1."}, {"label": "B", "text": "Biển 2."}, {"label": "C", "text": "Biển 3."}]'::jsonb, 'C', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (332, 5, 'Gặp biển nào xe xích lô được phép đi vào?', '[{"label": "A", "text": "Biển 1."}, {"label": "B", "text": "Biển 2."}, {"label": "C", "text": "Biển 3."}, {"label": "D", "text": "Biển 1 và biển 2."}]'::jsonb, 'D', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (333, 5, 'Gặp biển nào xe lam, xe xích lô máy được phép đi vào?', '[{"label": "A", "text": "Biển 1."}, {"label": "B", "text": "Biển 2."}, {"label": "C", "text": "Biển 3."}]'::jsonb, 'C', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (334, 5, 'Biển báo này có ý nghĩa như thế nào?', '[{"label": "A", "text": "Đường cấm súc vật vận tải hàng hóa."}, {"label": "B", "text": "Đường cấm súc vật vận tải hành khách."}, {"label": "C", "text": "Đường cấm súc vật vận tải hàng hóa hoặc hành khách dù kéo xe hay chở trên lưng đi qua."}]'::jsonb, 'C', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (335, 5, 'Biển báo này có ý nghĩa như thế nào?', '[{"label": "A", "text": "Tốc độ tối đa cho phép về ban đêm cho các phương tiện là 70 km/h."}, {"label": "B", "text": "Tốc độ tối thiểu cho phép về ban đêm cho các phương tiện là 70 km/h."}]'::jsonb, 'A', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (336, 5, 'Khi gặp các biển này xe ưu tiên theo luật định (có tải trọng hay chiều cao toàn bộ vượt quá chỉ số ghi trên biển) có được phép đi qua hay không?', '[{"label": "A", "text": "Được phép."}, {"label": "B", "text": "Không được phép."}]'::jsonb, 'B', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (337, 5, 'Biển nào hạn chế chiều cao của xe và hàng?', '[{"label": "A", "text": "Biển 1."}, {"label": "B", "text": "Biển 2."}, {"label": "C", "text": "Biển 3."}]'::jsonb, 'A', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (338, 5, 'Biển số 2 có ý nghĩa như thế nào?', '[{"label": "A", "text": "Cho phép xe ô tô có tải trọng trục lớn hơn 7 tấn đi qua."}, {"label": "B", "text": "Cho phép xe ô tô có tải trọng trên trục xe từ 7 tấn trở xuống đi qua."}]'::jsonb, 'B', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (339, 5, 'Biển số 3 có ý nghĩa như thế nào?', '[{"label": "A", "text": "Cấm các loại xe có tải trọng toàn bộ trên 10 tấn đi qua."}, {"label": "B", "text": "Hạn chế khối lượng hàng hóa chở trên xe."}, {"label": "C", "text": "Hạn chế tải trọng trên trục xe."}]'::jsonb, 'A', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (340, 5, 'Biển nào cấm máy kéo kéo theo rơ moóc?', '[{"label": "A", "text": "Biển 1."}, {"label": "B", "text": "Biển 2."}, {"label": "C", "text": "Cả hai biển."}]'::jsonb, 'C', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (341, 5, 'Khi gặp biển số 1, xe ô tô tải có được đi vào không?', '[{"label": "A", "text": "Được đi vào."}, {"label": "B", "text": "Không được đi vào."}]'::jsonb, 'A', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (342, 5, 'Biển nào không có hiệu lực đối với xe ô tô tải không kéo moóc?', '[{"label": "A", "text": "Biển 1 và biển 2."}, {"label": "B", "text": "Biển 2 và biển 3."}, {"label": "C", "text": "Biển 1 và biển 3."}, {"label": "D", "text": "Cả ba biển."}]'::jsonb, 'A', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (343, 5, 'Biển nào cấm máy kéo?', '[{"label": "A", "text": "Biển 1 và biển 2."}, {"label": "B", "text": "Biển 1 và biển 3."}, {"label": "C", "text": "Biển 2 và biển 3."}, {"label": "D", "text": "Cả ba biển."}]'::jsonb, 'C', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (344, 5, 'Khi gặp biển này, xe mô tô ba bánh chở hàng có được phép rẽ trái hoặc rẽ phải hay không?', '[{"label": "A", "text": "Được phép."}, {"label": "B", "text": "Không được phép."}]'::jsonb, 'B', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (345, 5, 'Biển này có hiệu lực đối với xe mô tô hai bánh, ba bánh chở hàng không?', '[{"label": "A", "text": "Có."}, {"label": "B", "text": "Không."}]'::jsonb, 'A', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (346, 5, 'Biển này có ý nghĩa như thế nào?', '[{"label": "A", "text": "Cấm xe cơ giới (trừ xe ưu tiên theo luật định) đi thẳng."}, {"label": "B", "text": "Cấm các loại xe cơ giới và xe mô tô (trừ xe ưu tiên theo luật định) đi về bên trái và bên phải."}, {"label": "C", "text": "Hướng trái và phải không cấm xe cơ giới."}]'::jsonb, 'B', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (347, 5, 'Biển phụ đặt dưới biển cấm bóp còi có ý nghĩa như thế nào?', '[{"label": "A", "text": "Báo khoảng cách đến nơi cấm bóp còi."}, {"label": "B", "text": "Chiều dài đoạn đường cấm bóp còi từ nơi đặt biển."}, {"label": "C", "text": "Báo cấm dùng còi có độ vang xa 500m."}]'::jsonb, 'B', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (348, 5, 'Chiều dài đoạn đường 500 m từ nơi đặt biển này, người lái xe có được phép bấm còi không?', '[{"label": "A", "text": "Được phép."}, {"label": "B", "text": "Không được phép."}]'::jsonb, 'B', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (349, 5, 'Biển nào xe mô tô hai bánh được đi vào?', '[{"label": "A", "text": "Biển 1 và biển 2."}, {"label": "B", "text": "Biển 1 và biển 3."}, {"label": "C", "text": "Biển 2 và biển 3."}]'::jsonb, 'B', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (350, 5, 'Biển nào xe mô tô hai bánh không được đi vào?', '[{"label": "A", "text": "Biển 1."}, {"label": "B", "text": "Biển 2."}, {"label": "C", "text": "Biển 3."}]'::jsonb, 'B', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (351, 5, 'Ba biển này có hiệu lực như thế nào?', '[{"label": "A", "text": "Cấm các loại xe ở biển phụ đi vào."}, {"label": "B", "text": "Cấm các loại xe cơ giới đi vào trừ loại xe ở biển phụ."}]'::jsonb, 'A', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (352, 5, 'Biển nào báo hiệu chiều dài đoạn đường phải giữ cự ly tối thiểu giữa hai xe?', '[{"label": "A", "text": "Biển 1. 2. Biển 2. 3. Cả hai biển."}]'::jsonb, 'A', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (353, 5, 'Biển nào báo hiệu khoảng cách thực tế từ nơi đặt biển đến nơi cần cự ly tối thiểu giữa hai xe?', '[{"label": "A", "text": "Biển 1."}, {"label": "B", "text": "Biển 2."}, {"label": "C", "text": "Cả hai biển."}]'::jsonb, 'B', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (354, 5, 'Biển này có ý nghĩa như thế nào?', '[{"label": "A", "text": "Cấm dừng xe về hướng bên trái."}, {"label": "B", "text": "Cấm dừng và đỗ xe theo hướng bên phải."}, {"label": "C", "text": "Được phép đỗ xe và dừng xe theo hướng bên phải."}]'::jsonb, 'B', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (355, 5, 'Theo hướng bên phải có được phép đỗ xe, dừng xe không?', '[{"label": "A", "text": "Không được phép."}, {"label": "B", "text": "Được phép."}]'::jsonb, 'A', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (356, 5, 'Gặp biển này, xe ô tô sơ mi rơ moóc có chiều dài toàn bộ kể cả xe, moóc và hàng lớn hơn trị số ghi trên biển có được phép đi vào hay không?', '[{"label": "A", "text": "Không được phép."}, {"label": "B", "text": "Được phép."}]'::jsonb, 'A', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (357, 5, 'Xe ô tô chở hàng vượt quá phía trước và sau thùng xe, mỗi phía quá 10% chiều dài toàn bộ thân xe, tổng chiều dài xe (cả hàng) từ trước đến sau nhỏ hơn trị số ghi trên biển thì có được phép đi vào không?', '[{"label": "A", "text": "Không được phép."}, {"label": "B", "text": "Được phép."}]'::jsonb, 'A', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (358, 5, 'Biển này có ý nghĩa như thế nào?', '[{"label": "A", "text": "Cấm ô tô buýt."}, {"label": "B", "text": "Cấm xe ô tô khách."}, {"label": "C", "text": "Cấm xe ô tô con."}]'::jsonb, 'B', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (359, 5, 'Biển này có ý nghĩa như thế nào?', '[{"label": "A", "text": "Hạn chế chiều cao của xe và hàng."}, {"label": "B", "text": "Hạn chế chiều ngang của xe và hàng."}, {"label": "C", "text": "Hạn chế chiều dài của xe và hàng."}]'::jsonb, 'B', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (360, 5, 'Biển nào là biển "Tốc độ tối đa cho phép về ban đêm"?', '[{"label": "A", "text": "Biển 1."}, {"label": "B", "text": "Biển 2."}, {"label": "C", "text": "Cả hai biển."}]'::jsonb, 'A', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (361, 5, 'Biển báo nào báo hiệu bắt đầu đoạn đường vào phạm vi khu dân cư, các phương tiện tham gia giao thông phải tuân theo các quy định đi đường được áp dụng ở khu đông dân cư?', '[{"label": "A", "text": "Biển 1."}, {"label": "B", "text": "Biển 2."}]'::jsonb, 'A', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (362, 5, 'Biển nào báo hiệu hạn chế tốc độ của phương tiện không vượt quá trị số ghi trên biển? 1', '[{"label": "A", "text": "Biển 1."}, {"label": "B", "text": "Biển 2."}]'::jsonb, 'B', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (363, 5, 'Trong các biển báo dưới đây biển nào báo hiệu "Kết thúc đường cao tốc"?', '[{"label": "A", "text": "Biển 1."}, {"label": "B", "text": "Biển 2."}, {"label": "C", "text": "Biển 3."}]'::jsonb, 'B', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (364, 5, 'Số 50 ghi trên biển báo dưới đây có ý nghĩa như thế nào?', '[{"label": "A", "text": "Tốc độ tối đa các xe cơ giới được phép chạy."}, {"label": "B", "text": "Tốc độ tối thiểu các xe cơ giới được phép chạy."}]'::jsonb, 'A', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (365, 5, 'Biển nào dưới đây chỉ dẫn bắt đầu đường cao tốc phân làn đường có tốc độ khác nhau?', '[{"label": "A", "text": "Biển 1."}, {"label": "B", "text": "Biển 2."}, {"label": "C", "text": "Cả hai biển."}]'::jsonb, 'B', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (366, 5, 'Biển báo dưới đây có ý nghĩa như thế nào?', '[{"label": "A", "text": "Báo hiệu tốc độ tối đa cho phép các xe cơ giới chạy."}, {"label": "B", "text": "Báo hiệu tốc độ tối thiểu cho phép các xe cơ giới chạy."}]'::jsonb, 'B', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (367, 5, 'Gặp biển nào người lái xe phải nhường đường cho người đi bộ?', '[{"label": "A", "text": "Biển 1."}, {"label": "B", "text": "Biển 2."}, {"label": "C", "text": "Biển 3."}]'::jsonb, 'A', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (368, 5, 'Biển nào chỉ đường dành cho người đi bộ, các loại xe không được đi vào khi gặp biển này?', '[{"label": "A", "text": "Biển 1."}, {"label": "B", "text": "Biển 1 và biển 3."}, {"label": "C", "text": "Biển 3."}, {"label": "D", "text": "Cả ba biển."}]'::jsonb, 'C', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (369, 5, 'Biển nào báo hiệu "Đường dành cho xe thô sơ"?', '[{"label": "A", "text": "Biển 1."}, {"label": "B", "text": "Biển 2."}, {"label": "C", "text": "Biển 3."}]'::jsonb, 'A', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (370, 5, 'Biển nào báo hiệu sắp đến chỗ giao nhau nguy hiểm?', '[{"label": "A", "text": "Biển 1."}, {"label": "B", "text": "Biển 1 và biển 2."}, {"label": "C", "text": "Biển 2 và biển 3."}, {"label": "D", "text": "Cả ba biển."}]'::jsonb, 'D', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (371, 5, 'Biển nào báo hiệu "Giao nhau với đường sắt có rào chắn"?', '[{"label": "A", "text": "Biển 1."}, {"label": "B", "text": "Biển 2 và biển 3."}, {"label": "C", "text": "Biển 3."}]'::jsonb, 'A', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (372, 5, 'Biển nào báo hiệu "Giao nhau có tín hiệu đèn"?', '[{"label": "A", "text": "Biển 1."}, {"label": "B", "text": "Biển 2."}, {"label": "C", "text": "Biển 3."}, {"label": "D", "text": "Cả ba biển."}]'::jsonb, 'C', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (373, 5, 'Biển nào báo hiệu nguy hiểm giao nhau với đường sắt?', '[{"label": "A", "text": "Biển 1 và biển 2."}, {"label": "B", "text": "Biển 1 và biển 3."}, {"label": "C", "text": "Biển 2 và biển 3."}, {"label": "D", "text": "Cả ba biển."}]'::jsonb, 'B', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (374, 5, 'Biển nào báo hiệu đường bộ giao nhau với đường sắt không có rào chắn?', '[{"label": "A", "text": "Biển 1 và biển 2."}, {"label": "B", "text": "Biển 1 và biển 3."}, {"label": "C", "text": "Biển 2 và biển 3."}, {"label": "D", "text": "Cả ba biển."}]'::jsonb, 'C', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (375, 5, 'Biển nào báo hiệu sắp đến chỗ giao nhau giữa đường bộ và đường sắt?', '[{"label": "A", "text": "Biển 1."}, {"label": "B", "text": "Biển 2."}, {"label": "C", "text": "Biển 3."}, {"label": "D", "text": "Biển 1 và biển 3."}]'::jsonb, 'A', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (376, 5, 'Biển nào báo hiệu "Cửa chui"?', '[{"label": "A", "text": "Biển 1."}, {"label": "B", "text": "Biển 2."}, {"label": "C", "text": "Biển 3."}]'::jsonb, 'B', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (377, 5, 'Hai biển này có ý nghĩa như thế nào?', '[{"label": "A", "text": "Để chỉ nơi đường sắt giao vuông góc với đường bộ không có rào chắn."}, {"label": "B", "text": "Để báo trước sắp đến vị trí giao cắt đường bộ với đường sắt cùng mức, không vuông góc và không có người gác, không có rào chắn."}, {"label": "C", "text": "Nơi đường sắt giao nhau với đường bộ."}]'::jsonb, 'A', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (378, 5, 'Biển nào báo hiệu "Nơi đường sắt giao vuông góc với đường bộ"?', '[{"label": "A", "text": "Biển 1."}, {"label": "B", "text": "Biển 2."}, {"label": "C", "text": "Biển 3."}, {"label": "D", "text": "Biển 1 và biển 2."}]'::jsonb, 'D', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (379, 5, 'Các biển này có ý nghĩa như thế nào?', '[{"label": "A", "text": "Nơi đường sắt giao nhau với đường bộ."}, {"label": "B", "text": "Nơi đường sắt giao vuông góc với đường bộ."}, {"label": "C", "text": "Để báo trước sắp đến vị trí đường sắt giao không vuông góc với đường bộ, không có người gác và không có rào chắn."}]'::jsonb, 'C', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (380, 5, 'Biển nào báo hiệu "Hết đoạn đường ưu tiên"?', '[{"label": "A", "text": "Biển 1."}, {"label": "B", "text": "Biển 2."}, {"label": "C", "text": "Biển 3."}]'::jsonb, 'C', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (381, 5, 'Biển nào báo hiệu, chỉ dẫn xe đi trên đường này được quyền ưu tiên qua nơi giao nhau?', '[{"label": "A", "text": "Biển 1 và biển 2."}, {"label": "B", "text": "Biển 1 và biển 3."}, {"label": "C", "text": "Biển 2 và biển 3."}, {"label": "D", "text": "Cả ba biển."}]'::jsonb, 'B', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (382, 5, 'Biển nào báo hiệu "Giao nhau với đường không ưu tiên"?', '[{"label": "A", "text": "Biển 1."}, {"label": "B", "text": "Biển 2."}, {"label": "C", "text": "Biển 3."}, {"label": "D", "text": "Biển 2 và biển 3."}]'::jsonb, 'A', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (383, 5, 'Biển nào báo hiệu "Giao nhau với đường ưu tiên"?', '[{"label": "A", "text": "Biển 1 và biển 3."}, {"label": "B", "text": "Biển 2."}, {"label": "C", "text": "Biển 3."}]'::jsonb, 'B', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (384, 5, 'Biển nào báo hiệu "Đường bị thu hẹp"?', '[{"label": "A", "text": "Biển 1 và biển 2."}, {"label": "B", "text": "Biển 1 và biển 3."}, {"label": "C", "text": "Biển 2 và biển 3."}, {"label": "D", "text": "Cả ba biển."}]'::jsonb, 'A', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (385, 5, 'Khi gặp biển nào, người lái xe phải giảm tốc độ, chú ý xe đi ngược chiều, xe đi ở phía đường bị hẹp phải nhường đường cho xe đi ngược chiều?', '[{"label": "A", "text": "Biển 1."}, {"label": "B", "text": "Biển 1 và biển 3."}, {"label": "C", "text": "Biển 2 và biển 3."}, {"label": "D", "text": "Cả ba biển."}]'::jsonb, 'C', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (386, 5, 'Biển nào báo hiệu "Giao nhau với đường ưu tiên"?', '[{"label": "A", "text": "Biển 1."}, {"label": "B", "text": "Biển 2."}, {"label": "C", "text": "Biển 3."}, {"label": "D", "text": "Cả ba biển."}]'::jsonb, 'C', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (387, 5, 'Biển nào báo hiệu "Đường giao nhau" của các tuyến đường cùng cấp?', '[{"label": "A", "text": "Biển 1."}, {"label": "B", "text": "Biển 2."}, {"label": "C", "text": "Biển 3."}]'::jsonb, 'A', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (388, 5, 'Biển nào báo hiệu "Giao nhau với đường không ưu tiên"?', '[{"label": "A", "text": "Biển 1."}, {"label": "B", "text": "Biển 2."}, {"label": "C", "text": "Biển 3."}]'::jsonb, 'B', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (389, 5, 'Biển nào báo hiệu "Đường hai chiều"?', '[{"label": "A", "text": "Biển 1."}, {"label": "B", "text": "Biển 2."}, {"label": "C", "text": "Biển 3."}]'::jsonb, 'A', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (390, 5, 'Biển nào báo hiệu phải giảm tốc độ, nhường đường cho xe cơ giới đi ngược chiều qua đường hẹp?', '[{"label": "A", "text": "Biển 1."}, {"label": "B", "text": "Biển 2."}, {"label": "C", "text": "Biển 3."}]'::jsonb, 'B', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (391, 5, 'Biển nào chỉ dẫn "Được ưu tiên qua đường hẹp"?', '[{"label": "A", "text": "Biển 1."}, {"label": "B", "text": "Biển 2."}, {"label": "C", "text": "Biển 3."}, {"label": "D", "text": "Biển 2 và biển 3."}]'::jsonb, 'C', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (392, 5, 'Biển nào báo hiệu "Đường đôi"?', '[{"label": "A", "text": "Biển 1."}, {"label": "B", "text": "Biển 2."}, {"label": "C", "text": "Biển 3."}]'::jsonb, 'B', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (393, 5, 'Biển nào báo hiệu "Đường đôi"?', '[{"label": "A", "text": "Biển 1."}, {"label": "B", "text": "Biển 2."}, {"label": "C", "text": "Biển 3."}]'::jsonb, 'C', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (394, 5, 'Biển nào báo hiệu "Kết thúc đường đôi"?', '[{"label": "A", "text": "Biển 1."}, {"label": "B", "text": "Biển 2."}, {"label": "C", "text": "Biển 3."}]'::jsonb, 'C', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (395, 5, 'Biển nào báo hiệu "Giao nhau với đường hai chiều"?', '[{"label": "A", "text": "Biển 1."}, {"label": "B", "text": "Biển 2."}, {"label": "C", "text": "Biển 3."}]'::jsonb, 'A', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (396, 5, 'Biển nào báo hiệu "Đường hai chiều"?', '[{"label": "A", "text": "Biển 1."}, {"label": "B", "text": "Biển 2."}, {"label": "C", "text": "Biển 3."}]'::jsonb, 'B', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (397, 5, 'Biển nào báo hiệu "Giao nhau với đường hai chiều"?', '[{"label": "A", "text": "Biển 1."}, {"label": "B", "text": "Biển 2."}, {"label": "C", "text": "Biển 3."}]'::jsonb, 'B', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (398, 5, 'Biển nào báo hiệu "Chú ý chướng ngại vật"?', '[{"label": "A", "text": "Biển 1."}, {"label": "B", "text": "Biển 2 và biển 3."}, {"label": "C", "text": "Cả ba biển."}]'::jsonb, 'B', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (399, 5, 'Biển nào chỉ dẫn cho người tham gia giao thông biết vị trí và khoảng cách có làn đường cứu nạn hay làn thoát xe khẩn cấp?', '[{"label": "A", "text": "Biển 1."}, {"label": "B", "text": "Biển 2."}, {"label": "C", "text": "Cả hai biển."}]'::jsonb, 'B', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (400, 5, 'Biển nào báo hiệu "Đường hầm"?', '[{"label": "A", "text": "Cả ba biển."}, {"label": "B", "text": "Biển 2."}, {"label": "C", "text": "Biển 2 và biển 3."}]'::jsonb, 'B', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (401, 5, 'Biển nào dưới đây là biển "Cầu hẹp"?', '[{"label": "A", "text": "Biển 1."}, {"label": "B", "text": "Biển 2."}, {"label": "C", "text": "Biển 3."}]'::jsonb, 'B', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (402, 5, 'Biển nào dưới đây là biển "Cầu quay - cầu cất"?', '[{"label": "A", "text": "Biển 1."}, {"label": "B", "text": "Biển 2."}, {"label": "C", "text": "Biển 3."}]'::jsonb, 'C', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (403, 5, 'Biển nào dưới đây là biển "Kè, vực sâu phía trước"?', '[{"label": "A", "text": "Biển 1."}, {"label": "B", "text": "Biển 2."}, {"label": "C", "text": "Biển 3."}]'::jsonb, 'A', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (404, 5, 'Biển nào sau đây là biển "Kè, vực sâu bên đường phía bên trái"?', '[{"label": "A", "text": "Biển 1."}, {"label": "B", "text": "Biển 2."}, {"label": "C", "text": "Biển 3."}, {"label": "D", "text": "Biển 1 và biển 2."}]'::jsonb, 'C', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (405, 5, 'Biển nào sau đây là biển "Kè, vực sâu bên đường phía bên phải"?', '[{"label": "A", "text": "Biển 1."}, {"label": "B", "text": "Biển 2."}, {"label": "C", "text": "Biển 3."}]'::jsonb, 'B', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (406, 5, 'Biển nào sau đây là biển "Đường trơn"?', '[{"label": "A", "text": "Biển 1."}, {"label": "B", "text": "Biển 2."}, {"label": "C", "text": "Biển 3."}]'::jsonb, 'B', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (407, 5, 'Biển nào sau đây là biển "Lề đường nguy hiểm"?', '[{"label": "A", "text": "Biển 1."}, {"label": "B", "text": "Biển 2."}, {"label": "C", "text": "Biển 3."}]'::jsonb, 'B', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (408, 5, 'Biển nào sau đây báo trước gần tới đoạn đường đang tiến hành thi công sửa chữa?', '[{"label": "A", "text": "Biển 1."}, {"label": "B", "text": "Biển 2."}, {"label": "C", "text": "Biển 3."}, {"label": "D", "text": "Không biển nào."}]'::jsonb, 'A', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (409, 5, 'Biển nào sau đây cảnh báo nguy hiểm đoạn đường thường xảy ra tai nạn?', '[{"label": "A", "text": "Biển 1."}, {"label": "B", "text": "Biển 2."}, {"label": "C", "text": "Biển 3."}]'::jsonb, 'B', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (410, 5, 'Biển nào dưới đây báo hiệu đoạn đường hay xảy ra ùn tắc giao thông?', '[{"label": "A", "text": "Biển 1."}, {"label": "B", "text": "Biển 2."}, {"label": "C", "text": "Biển 3."}, {"label": "D", "text": "Cả ba biển."}]'::jsonb, 'B', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (411, 5, 'Gặp biển nào người tham gia giao thông phải đi chậm và thận trọng đề phòng khả năng xuất hiện và di chuyển bất ngờ của trẻ em trên mặt đường?', '[{"label": "A", "text": "Biển 1."}, {"label": "B", "text": "Biển 2."}]'::jsonb, 'B', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (412, 5, 'Biển nào chỉ dẫn nơi bắt đầu đoạn đường dành cho người đi bộ?', '[{"label": "A", "text": "Biển 1."}, {"label": "B", "text": "Biển 2."}, {"label": "C", "text": "Biển 3."}]'::jsonb, 'B', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (413, 5, 'Biển nào dưới đây báo hiệu gần đến đoạn đường thường có trẻ em đi ngang qua?', '[{"label": "A", "text": "Biển 1."}, {"label": "B", "text": "Biển 2."}, {"label": "C", "text": "Biển 3."}, {"label": "D", "text": "Cả ba biển."}]'::jsonb, 'B', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (414, 5, 'Gặp biển nào dưới đây người tham gia giao thông cần phải điều chỉnh tốc độ xe chạy cho thích hợp, đề phòng gió ngang thổi mạnh gây lật xe?', '[{"label": "A", "text": "Biển 1."}, {"label": "B", "text": "Biển 2."}, {"label": "C", "text": "Cả hai biển."}]'::jsonb, 'A', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (415, 5, 'Biển nào sau đây là biển "Dốc xuống nguy hiểm"?', '[{"label": "A", "text": "Biển 1."}, {"label": "B", "text": "Biển 2."}]'::jsonb, 'A', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (416, 5, 'Biển nào sau đây là biển "Dốc lên nguy hiểm"?', '[{"label": "A", "text": "Biển 1."}, {"label": "B", "text": "Biển 2."}]'::jsonb, 'B', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (417, 5, 'Biển báo này có ý nghĩa như thế nào?', '[{"label": "A", "text": "Chỗ ngoặt nguy hiểm có nguy cơ lật xe bên phải khi đường cong vòng sang trái."}, {"label": "B", "text": "Chỗ ngoặt nguy hiểm có nguy cơ lật xe bên trái khi đường cong vòng sang phải."}]'::jsonb, 'A', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (418, 5, 'Biển báo này có ý nghĩa như thế nào?', '[{"label": "A", "text": "Báo trước đoạn đường có gió ngang."}, {"label": "B", "text": "Báo trước đoạn đường trơn trượt."}, {"label": "C", "text": "Báo trước sắp đến bến phà."}]'::jsonb, 'C', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (419, 5, 'Biển báo này có ý nghĩa như thế nào?', '[{"label": "A", "text": "Báo hiệu đường có ổ gà, lồi lõm."}, {"label": "B", "text": "Báo hiệu đường có gồ giảm tốc phía trước."}]'::jsonb, 'A', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (420, 5, 'Biển báo này có ý nghĩa như thế nào?', '[{"label": "A", "text": "Báo hiệu đường có ổ gà, lồi lõm."}, {"label": "B", "text": "Báo hiệu đường có gồ giảm tốc phía trước."}]'::jsonb, 'B', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (421, 5, 'Biển báo dưới đây có ý nghĩa như thế nào?', '[{"label": "A", "text": "Để báo trước gần tới đoạn đường có hiện tượng đất đá từ trên ta luy dương sụt lở bất ngờ gây nguy hiểm cho xe cộ và người đi đường."}, {"label": "B", "text": "Để báo trước nơi có kết cấu mặt đường rời rạc, khi phương tiện đi qua, làm cho các viên đá, sỏi văng lên gây nguy hiểm và mất an toàn cho người và phương tiện tham gia giao thông."}, {"label": "C", "text": "Để cảnh báo những đoạn nền đường yếu, đoạn đường đang theo dõi lún mà việc vận hành xe ở tốc độ cao có thể gây nguy hiểm."}]'::jsonb, 'C', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (422, 5, 'Các biển báo này có ý nghĩa như thế nào?', '[{"label": "A", "text": "Để báo trước gần tới đoạn đường có hiện tượng đất đá từ trên ta luy dương sụt lở bất ngờ gây nguy hiểm cho xe cộ và người đi đường."}, {"label": "B", "text": "Để báo trước nơi có kết cấu mặt đường rời rạc, khi phương tiện đi qua, làm cho các viên đá, sỏi văng lên gây nguy hiểm và mất an toàn cho người và phương tiện tham gia giao thông."}, {"label": "C", "text": "Để cảnh báo những đoạn nền đường yếu, đoạn đường đang theo dõi lún mà việc vận hành xe ở tốc độ cao có thể gây nguy hiểm."}]'::jsonb, 'A', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (423, 5, 'Biển báo này có ý nghĩa như thế nào?', '[{"label": "A", "text": "Để báo trước gần tới đoạn đường có hiện tượng đất đá từ trên ta luy dương sụt lở bất ngờ gây nguy hiểm cho xe cộ và người đi đường."}, {"label": "B", "text": "Để báo trước nơi có kết cấu mặt đường rời rạc, khi phương tiện đi qua, làm cho các viên đá, sỏi văng lên gây nguy hiểm và mất an toàn cho người và phương tiện tham gia giao thông."}, {"label": "C", "text": "Để cảnh báo những đoạn nền đường yếu, đoạn đường đang theo dõi lún mà việc vận hành xe ở tốc độ cao có thể gây nguy hiểm."}]'::jsonb, 'B', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (424, 5, 'Gặp biển báo này người lái xe phải xử lý thế nào?', '[{"label": "A", "text": "Đi chậm, quan sát và dừng lại nếu gặp gia súc trên đường."}, {"label": "B", "text": "Bấm còi to để gia súc tránh đường và nhanh chóng di chuyển qua đoạn đường có gia súc."}]'::jsonb, 'A', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (425, 5, 'Biển báo này có ý nghĩa như thế nào?', '[{"label": "A", "text": "Báo hiệu khu vực nguy hiểm thường xuyên có sét đánh."}, {"label": "B", "text": "Báo hiệu khu vực có đường dây điện cắt ngang phía trên tuyến đường."}]'::jsonb, 'B', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (426, 5, 'Biển nào báo hiệu các phương tiện phải đi đúng làn đường quy định và tuân thủ tốc độ tối đa cho phép?', '[{"label": "A", "text": "Biển 1."}, {"label": "B", "text": "Biển 2."}, {"label": "C", "text": "Cả hai biển."}]'::jsonb, 'B', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (427, 5, 'Biển nào báo hiệu các phương tiện phải tuân thủ tốc độ tối đa cho phép trên từng làn đường?', '[{"label": "A", "text": "Biển 1."}, {"label": "B", "text": "Biển 2."}, {"label": "C", "text": "Cả hai biển."}]'::jsonb, 'C', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (428, 5, 'Biển nào báo hiệu "Đường dành cho xe ô tô"?', '[{"label": "A", "text": "Biển 1."}, {"label": "B", "text": "Biển 2."}]'::jsonb, 'A', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (429, 5, 'Biển nào báo hiệu "Hết đoạn đường dành cho xe ô tô"?', '[{"label": "A", "text": "Biển 1."}, {"label": "B", "text": "Biển 2."}]'::jsonb, 'B', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (430, 5, 'Khi gặp biển nào thì các phương tiện không được đi vào, trừ xe ô tô và xe mô tô?', '[{"label": "A", "text": "Biển 1."}, {"label": "B", "text": "Biển 2."}]'::jsonb, 'A', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (431, 5, 'Biển này có ý nghĩa như thế nào?', '[{"label": "A", "text": "Chỉ hướng đi phải theo."}, {"label": "B", "text": "Biển báo hiệu cho người lái xe biết số lượng làn đường trên mặt đường và hướng đi trên mỗi làn đường phải theo."}, {"label": "C", "text": "Chỉ hướng đường phải theo."}]'::jsonb, 'B', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (432, 5, 'Biển nào (đặt trước ngã ba, ngã tư) cho phép xe được rẽ sang hướng khác?', '[{"label": "A", "text": "Biển 1."}, {"label": "B", "text": "Biển 2."}, {"label": "C", "text": "Không biển nào."}]'::jsonb, 'C', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (433, 5, 'Biển nào báo hiệu "Hướng đi thẳng phải theo"?', '[{"label": "A", "text": "Biển 1."}, {"label": "B", "text": "Biển 2."}]'::jsonb, 'A', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (434, 5, 'Biển nào báo hiệu "Đường một chiều"?', '[{"label": "A", "text": "Biển 1."}, {"label": "B", "text": "Biển 2."}, {"label": "C", "text": "Cả hai biển."}]'::jsonb, 'B', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (435, 5, 'Trong các biển dưới đây biển nào là biển "Hết tốc độ tối đa cho phép"?', '[{"label": "A", "text": "Biển 1."}, {"label": "B", "text": "Biển 2."}, {"label": "C", "text": "Biển 3."}, {"label": "D", "text": "Cả ba biển."}]'::jsonb, 'A', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (436, 5, 'Hiệu lực của biển "Tốc độ tối đa cho phép" hết tác dụng khi gặp biển nào dưới đây?', '[{"label": "A", "text": "Biển 1."}, {"label": "B", "text": "Biển 2."}, {"label": "C", "text": "Biển 3."}, {"label": "D", "text": "Biển 1 và biển 2."}]'::jsonb, 'D', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (437, 5, 'Trong các biển dưới đây biển nào là biển "Hết tốc độ tối thiểu"?', '[{"label": "A", "text": "Biển 1."}, {"label": "B", "text": "Biển 2."}, {"label": "C", "text": "Biển 3."}, {"label": "D", "text": "Cả ba biển."}]'::jsonb, 'C', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (438, 5, 'Biển nào dưới đây báo hiệu hết cấm vượt?', '[{"label": "A", "text": "Biển 1."}, {"label": "B", "text": "Biển 2."}, {"label": "C", "text": "Biển 3."}, {"label": "D", "text": "Biển 2 và biển 3."}]'::jsonb, 'D', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (439, 5, 'Trong các biển dưới đây biển nào là biển "Hết mọi lệnh cấm"?', '[{"label": "A", "text": "Biển 1."}, {"label": "B", "text": "Biển 2."}, {"label": "C", "text": "Biển 3."}, {"label": "D", "text": "Cả ba biển."}]'::jsonb, 'B', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (440, 5, 'Biển nào cho phép được quay đầu xe đi theo hướng ngược lại khi đặt biển trước ngã ba, ngã tư?', '[{"label": "A", "text": "Biển 1."}, {"label": "B", "text": "Biển 2."}, {"label": "C", "text": "Biển 3."}, {"label": "D", "text": "Không biển nào."}]'::jsonb, 'C', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (441, 5, 'Biển nào không cho phép rẽ phải?', '[{"label": "A", "text": "Biển 1."}, {"label": "B", "text": "Biển 2."}, {"label": "C", "text": "Biển 3."}, {"label": "D", "text": "Biển 1 và biển 3."}]'::jsonb, 'A', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (442, 5, 'Khi đến chỗ giao nhau, gặp biển nào thì người lái xe không được cho xe đi thẳng, phải rẽ sang hướng khác?', '[{"label": "A", "text": "Biển 1 và biển 2."}, {"label": "B", "text": "Biển 1 và biển 3."}, {"label": "C", "text": "Biển 2 và biển 3."}, {"label": "D", "text": "Cả ba biển."}]'::jsonb, 'A', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (443, 5, 'Biển nào cho phép quay đầu xe?', '[{"label": "A", "text": "Biển 1."}, {"label": "B", "text": "Biển 2."}, {"label": "C", "text": "Cả hai biển."}]'::jsonb, 'C', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (444, 5, 'Biển nào chỉ dẫn tên đường trên các tuyến đường đối ngoại?', '[{"label": "A", "text": "Biển 1."}, {"label": "B", "text": "Biển 2."}, {"label": "C", "text": "Biển 3."}, {"label": "D", "text": "Biển 1 và biển 2."}]'::jsonb, 'C', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (445, 5, 'Biển số 1 có ý nghĩa như thế nào?', '[{"label": "A", "text": "Biển chỉ dẫn hết cấm đỗ xe theo giờ trong khu vực."}, {"label": "B", "text": "Biển chỉ dẫn hết hiệu lực khu vực đỗ xe trên các tuyến đường đối ngoại."}, {"label": "C", "text": "Biển chỉ dẫn khu vực đỗ xe trên các tuyến đường đối ngoại."}]'::jsonb, 'A', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (446, 5, 'Biển số 3 có ý nghĩa như thế nào?', '[{"label": "A", "text": "Biển chỉ dẫn khu vực cấm đỗ xe trên các tuyến đường đối ngoại."}, {"label": "B", "text": "Biển chỉ dẫn khu vực đỗ xe trên các tuyến đường đối ngoại."}, {"label": "C", "text": "Biển hạn chế tốc độ tối đa trong khu vực."}, {"label": "D", "text": "Biển chỉ dẫn hết hiệu lực khu vực cấm đỗ xe theo giờ trên các tuyến đường đối ngoại."}]'::jsonb, 'C', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (447, 5, 'Biển nào báo hiệu "Đường phía trước có làn đường dành cho ô tô khách"?', '[{"label": "A", "text": "Biển 1."}, {"label": "B", "text": "Biển 2."}, {"label": "C", "text": "Biển 3."}]'::jsonb, 'B', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (448, 5, 'Biển nào báo hiệu "Rẽ ra đường có làn đường dành cho ô tô khách"?', '[{"label": "A", "text": "Biển 1."}, {"label": "B", "text": "Biển 2."}, {"label": "C", "text": "Biển 3."}]'::jsonb, 'C', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (449, 5, 'Biển nào đặt trên đường chính trước khi đến nơi đường giao nhau để rẽ vào đường cụt?', '[{"label": "A", "text": "Biển 1 và 2."}, {"label": "B", "text": "Cả ba biển."}, {"label": "C", "text": "Không biển nào."}]'::jsonb, 'A', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (450, 5, 'Biển nào chỉ dẫn cho người đi bộ sử dụng cầu vượt qua đường?', '[{"label": "A", "text": "Biển 1."}, {"label": "B", "text": "Biển 2."}, {"label": "C", "text": "Cả hai biển."}, {"label": "D", "text": "Không biển nào."}]'::jsonb, 'A', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (451, 5, 'Biển nào chỉ dẫn cho người đi bộ sử dụng hầm chui qua đường?', '[{"label": "A", "text": "Biển 1."}, {"label": "B", "text": "Biển 2."}, {"label": "C", "text": "Cả hai biển."}, {"label": "D", "text": "Không biển nào."}]'::jsonb, 'B', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (452, 5, 'Biển nào báo hiệu "Nơi đỗ xe dành cho người khuyết tật"?', '[{"label": "A", "text": "Biển 1."}, {"label": "B", "text": "Biển 2."}, {"label": "C", "text": "Biển 3."}]'::jsonb, 'B', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (453, 5, 'Gặp biển báo này, người lái xe phải đỗ xe như thế nào?', '[{"label": "A", "text": "Đỗ xe hoàn toàn trên hè phố."}, {"label": "B", "text": "Đỗ xe hoàn toàn dưới lòng đường."}, {"label": "C", "text": "Đỗ từ 1/2 thân xe trở lên trên hè phố."}]'::jsonb, 'C', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (454, 5, 'Gặp biển báo này, người tham gia giao thông phải xử lý như thế nào?', '[{"label": "A", "text": "Dừng xe tại khu vực có trạm Cảnh sát giao thông."}, {"label": "B", "text": "Tiếp tục lưu thông với tốc độ bình thường."}, {"label": "C", "text": "Phải giảm tốc độ đến mức an toàn và không được vượt khi đi qua khu vực này."}]'::jsonb, 'C', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (455, 5, 'Gặp biển báo dưới đây, người lái xe có bắt buộc phải chạy vòng theo đảo an toàn theo hướng mũi tên khi muốn chuyển hướng hay không?', '[{"label": "A", "text": "Bắt buộc."}, {"label": "B", "text": "Không bắt buộc."}]'::jsonb, 'A', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (456, 5, 'Biển nào dưới đây chỉ dẫn địa giới hành chính giữa các thành phố, tỉnh, huyện?', '[{"label": "A", "text": "Biển 1."}, {"label": "B", "text": "Biển 2."}, {"label": "C", "text": "Biển 3."}]'::jsonb, 'B', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (457, 5, 'Biển nào báo hiệu "Cầu vượt liên thông"?', '[{"label": "A", "text": "Biển 2 và biển 3."}, {"label": "B", "text": "Biển 1 và biển 2."}, {"label": "C", "text": "Biển 1 và biển 3."}, {"label": "D", "text": "Cả ba biển."}]'::jsonb, 'C', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (458, 5, 'Biển số 1 có ý nghĩa như thế nào?', '[{"label": "A", "text": "Đi thẳng hoặc rẽ trái trên cầu vượt."}, {"label": "B", "text": "Đi thẳng hoặc rẽ phải trên cầu vượt."}, {"label": "C", "text": "Báo hiệu cầu vượt liên thông."}]'::jsonb, 'C', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (459, 5, 'Biển nào báo hiệu "Tuyến đường cầu vượt cắt qua"?', '[{"label": "A", "text": "Biển 1 và biển 2."}, {"label": "B", "text": "Biển 1 và biển 3."}, {"label": "C", "text": "Biển 2 và biển 3."}]'::jsonb, 'A', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (460, 5, 'Biển báo dưới đây có ý nghĩa như thế nào?', '[{"label": "A", "text": "Chỉ dẫn khoảng cách đến làn đường cứu nạn (làn thoát xe khẩn cấp)."}, {"label": "B", "text": "Báo hiệu đường cụt phía trước."}, {"label": "C", "text": "Báo hiệu nút giao gần nhất phía trước."}, {"label": "D", "text": "Báo hiệu trạm dừng nghỉ phía trước."}]'::jsonb, 'A', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (461, 5, 'Tại đoạn đường có biển "Làn đường dành riêng cho từng loại xe" dưới đây, các phương tiện có được phép chuyển sang làn khác để đi theo hành trình mong muốn khi đến gần nơi đường bộ giao nhau hay không?', '[{"label": "A", "text": "Được phép chuyển sang làn khác."}, {"label": "B", "text": "Không được phép chuyển sang làn khác, chỉ được đi trong làn quy định theo biển."}]'::jsonb, 'A', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (462, 5, 'Tại đoạn đường có cắm biển dưới đây, xe tải và xe khách có được phép đi vào không?', '[{"label": "A", "text": "Có."}, {"label": "B", "text": "Không."}]'::jsonb, 'A', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (463, 5, 'Tại đoạn đường có cắm biển dưới đây, xe tải và xe khách có được phép đi vào không?', '[{"label": "A", "text": "Có."}, {"label": "B", "text": "Không."}]'::jsonb, 'B', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (464, 5, 'Biển nào báo hiệu kết thúc đường cao tốc?', '[{"label": "A", "text": "Biển 1."}, {"label": "B", "text": "Biển 2."}, {"label": "C", "text": "Biển 3."}]'::jsonb, 'B', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (465, 5, 'Biển này có ý nghĩa như thế nào?', '[{"label": "A", "text": "Chỉ dẫn sắp đến vị trí nhập làn xe."}, {"label": "B", "text": "Chỉ dẫn vị trí nhập làn xe cách 250 m."}, {"label": "C", "text": "Chỉ dẫn vị trí nhập làn cách trạm thu phí 250 m."}]'::jsonb, 'B', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (466, 5, 'Biển nào chỉ dẫn nhập làn xe?', '[{"label": "A", "text": "Biển 1."}, {"label": "B", "text": "Biển 2."}, {"label": "C", "text": "Cả hai biển."}]'::jsonb, 'C', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (467, 5, 'Biển này có ý nghĩa như thế nào?', '[{"label": "A", "text": "Bắt đầu đường cao tốc."}, {"label": "B", "text": "Chỉ dẫn địa giới đường cao tốc, chiều dài đường cao tốc."}, {"label": "C", "text": "Tên và ký hiệu đường cao tốc, giá trị hạn chế tốc độ tối đa và tối thiểu."}, {"label": "D", "text": "Ý 1 và ý 3."}]'::jsonb, 'D', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (468, 5, 'Ý nghĩa các biểu tượng ghi trên biển chỉ dẫn là như thế nào?', '[{"label": "A", "text": "Xăng dầu, ăn uống, thông tin, sửa chữa xe."}, {"label": "B", "text": "Xăng dầu, ăn uống, nhà nghỉ, sửa chữa xe."}, {"label": "C", "text": "Xăng dầu, ăn uống, cấp cứu, sửa chữa xe."}]'::jsonb, 'A', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (469, 5, 'Biển này có ý nghĩa như thế nào?', '[{"label": "A", "text": "Chỉ dẫn đến trạm kiểm tra tải trọng xe."}, {"label": "B", "text": "Chỉ dẫn hướng rẽ vào nơi đặt trạm kiểm tra tải trọng xe."}, {"label": "C", "text": "Chỉ dẫn khoảng cách đến trạm kiểm tra tải trọng xe cách 750 m."}]'::jsonb, 'C', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (470, 5, 'Trên đường cao tốc, gặp biển nào thì người lái xe đi theo hướng bên trái để tránh chướng ngại vật?', '[{"label": "A", "text": "Biển 1."}, {"label": "B", "text": "Biển 2."}, {"label": "C", "text": "Biển 3."}]'::jsonb, 'A', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (471, 5, 'Trên đường cao tốc, gặp biển nào thì người lái xe đi theo hướng bên phải để tránh chướng ngại vật?', '[{"label": "A", "text": "Biển 1."}, {"label": "B", "text": "Biển 2."}, {"label": "C", "text": "Biển 3."}]'::jsonb, 'C', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (472, 5, 'Trên đường cao tốc, gặp biển nào thì người lái xe đi được cả hai hướng (bên trái hoặc bên phải) để tránh chướng ngại vật?', '[{"label": "A", "text": "Biển 1."}, {"label": "B", "text": "Biển 2."}, {"label": "C", "text": "Biển 3."}]'::jsonb, 'B', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (473, 5, 'Trên đường cao tốc, gặp biển nào người lái xe phải chú ý đổi hướng đi khi sắp vào đường cong nguy hiểm?', '[{"label": "A", "text": "Biển 1 và biển 2."}, {"label": "B", "text": "Biển 1 và biển 3."}, {"label": "C", "text": "Biển 2."}, {"label": "D", "text": "Biển 3."}]'::jsonb, 'B', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (474, 5, 'Biển nào chỉ dẫn người lái xe đi được cả hai hướng?', '[{"label": "A", "text": "Biển 1."}, {"label": "B", "text": "Biển 2."}]'::jsonb, 'B', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (475, 5, 'Biển này có ý nghĩa như thế nào?', '[{"label": "A", "text": "Chỉ dẫn chướng ngại vật phía trước để cảnh báo phía trước có sự cản trở lưu thông bình thường (nằm bên trong phần xe chạy hay ngay sát phần đường xe chạy) và chỉ dẫn hướng đi qua đó cần đặt biển."}, {"label": "B", "text": "Chỉ dẫn hướng rẽ để nhắc người điều khiển phương tiện chuẩn bị đổi hướng đi khi sắp vào đường cong nguy hiểm, có bán kính cong nhỏ."}]'::jsonb, 'B', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (476, 5, 'Vạch mũi tên chỉ hướng trên mặt đường nào dưới đây cho phép xe chỉ được đi thẳng và rẽ phải?', '[{"label": "A", "text": "Vạch 1."}, {"label": "B", "text": "Vạch 2 và vạch 3."}, {"label": "C", "text": "Vạch 3."}, {"label": "D", "text": "Vạch 1 và vạch 2."}]'::jsonb, 'C', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (477, 5, 'Vạch kẻ đường nào dưới đây là vạch phân chia các làn xe cùng chiều?', '[{"label": "A", "text": "Vạch 1."}, {"label": "B", "text": "Vạch 2."}, {"label": "C", "text": "Vạch 3."}, {"label": "D", "text": "Vạch 1 và vạch 2."}]'::jsonb, 'D', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (478, 5, 'Vạch kẻ đường nào dưới đây là vạch phân chia hai chiều xe chạy (vạch tim đường), xe không được lấn làn, không được đè lên vạch?', '[{"label": "A", "text": "Vạch 1."}, {"label": "B", "text": "Vạch 2."}, {"label": "C", "text": "Vạch 3."}, {"label": "D", "text": "Cả ba vạch."}]'::jsonb, 'B', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (479, 5, 'Vạch kẻ đường nào dưới đây là vạch phân chia hai chiều xe chạy (vạch tim đường)?', '[{"label": "A", "text": "Vạch 1."}, {"label": "B", "text": "Vạch 2."}, {"label": "C", "text": "Vạch 3."}, {"label": "D", "text": "Vạch 1 và vạch 3."}]'::jsonb, 'D', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (480, 5, 'Các vạch dưới đây có tác dụng như thế nào?', '[{"label": "A", "text": "Phân chia hai chiều xe chạy ngược chiều nhau."}, {"label": "B", "text": "Phân chia các làn xe chạy cùng chiều nhau."}]'::jsonb, 'A', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (481, 5, 'Các vạch dưới đây có tác dụng như thế nào?', '[{"label": "A", "text": "Phân chia hai chiều xe chạy ngược chiều nhau."}, {"label": "B", "text": "Phân chia các làn xe chạy cùng chiều nhau."}]'::jsonb, 'B', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (482, 5, 'Khi gặp vạch kẻ đường nào các xe được phép đè vạch?', '[{"label": "A", "text": "Vạch 1."}, {"label": "B", "text": "Vạch 2."}, {"label": "C", "text": "Vạch 3."}, {"label": "D", "text": "Vạch 1 và vạch 3."}]'::jsonb, 'D', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (483, 5, 'Vạch dưới đây có tác dụng như thế nào?', '[{"label": "A", "text": "Để xác định làn đường."}, {"label": "B", "text": "Báo hiệu người lái xe chỉ được phép đi thẳng."}, {"label": "C", "text": "Dùng để xác định khoảng cách giữa các phương tiện trên đường."}]'::jsonb, 'C', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (484, 5, 'Vạch dưới đây có ý nghĩa như thế nào?', '[{"label": "A", "text": "Báo cho người điều khiển không được dừng phương tiện trong phạm vi phần mặt đường có bố trí vạch để tránh ùn tắc giao thông."}, {"label": "B", "text": "Báo hiệu sắp đến chỗ có bố trí vạch đi bộ qua đường."}, {"label": "C", "text": "Dùng để xác định khoảng cách giữa các phương tiện trên đường."}]'::jsonb, 'B', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (485, 5, 'Vạch dưới đây có ý nghĩa như thế nào?', '[{"label": "A", "text": "Vị trí dừng xe của các phương tiện vận tải hành khách công cộng."}, {"label": "B", "text": "Báo cho người điều khiển được dừng phương tiện trong phạm vi phần mặt đường có bố trí vạch để tránh ùn tắc giao thông."}, {"label": "C", "text": "Dùng để xác định vị trí giữa các phương tiện trên đường. 127"}]'::jsonb, 'A', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (486, 6, 'Theo hướng mũi tên, xe nào chấp hành đúng quy tắc giao thông?', '[{"label": "A", "text": "Xe khách, xe tải, xe mô tô."}, {"label": "B", "text": "Xe tải, xe mô tô."}, {"label": "C", "text": "Chỉ xe con."}]'::jsonb, 'C', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (487, 6, 'Theo hướng mũi tên, thứ tự các xe đi như thế nào là đúng quy tắc giao thông?', '[{"label": "A", "text": "Xe tải, xe khách, xe con, xe mô tô."}, {"label": "B", "text": "Xe tải, xe mô tô, xe khách, xe con."}, {"label": "C", "text": "Xe khách, xe tải, xe con, xe mô tô."}, {"label": "D", "text": "Xe mô tô, xe khách, xe tải, xe con."}]'::jsonb, 'B', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (488, 6, 'Theo hướng mũi tên, thứ tự các xe đi như thế nào là đúng quy tắc giao thông?', '[{"label": "A", "text": "Xe công an đi làm nhiệm vụ khẩn cấp, xe con, xe tải, xe khách."}, {"label": "B", "text": "Xe công an đi làm nhiệm vụ khẩn cấp, xe khách, xe con, xe tải."}, {"label": "C", "text": "Xe công an đi làm nhiệm vụ khẩn cấp, xe tải, xe khách, xe con."}, {"label": "D", "text": "Xe con, xe công an đi làm nhiệm vụ khẩn cấp, xe tải, xe khách."}]'::jsonb, 'A', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (489, 6, 'Theo hướng mũi tên, thứ tự các xe đi như thế nào là đúng quy tắc giao thông?', '[{"label": "A", "text": "Xe tải, xe công an đi làm nhiệm vụ khẩn cấp, xe khách, xe con."}, {"label": "B", "text": "Xe công an đi làm nhiệm vụ khẩn cấp, xe khách, xe con, xe tải."}, {"label": "C", "text": "Xe công an đi làm nhiệm vụ khẩn cấp, xe con, xe tải, xe khách."}, {"label": "D", "text": "Xe công an đi làm nhiệm vụ khẩn cấp, xe tải, xe khách, xe con."}]'::jsonb, 'D', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (490, 6, 'Theo hướng mũi tên, thứ tự các xe đi như thế nào là đúng quy tắc giao thông?', '[{"label": "A", "text": "Xe tải, xe con, xe mô tô."}, {"label": "B", "text": "Xe con, xe tải, xe mô tô."}, {"label": "C", "text": "Xe mô tô, xe con, xe tải."}, {"label": "D", "text": "Xe con, xe mô tô, xe tải."}]'::jsonb, 'C', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (491, 6, 'Xe nào phải nhường đường trong trường hợp này?', '[{"label": "A", "text": "Xe con."}, {"label": "B", "text": "Xe tải."}]'::jsonb, 'A', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (492, 6, 'Trường hợp này xe nào được quyền đi trước?', '[{"label": "A", "text": "Xe mô tô."}, {"label": "B", "text": "Xe con."}]'::jsonb, 'B', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (493, 6, 'Thứ tự các xe đi như thế nào là đúng quy tắc giao thông?', '[{"label": "A", "text": "Xe con (A), xe cứu thương đi làm nhiệm vụ cấp cứu, xe con (B)."}, {"label": "B", "text": "Xe cứu thương đi làm nhiệm vụ cấp cứu, xe con (B), xe con (A)."}, {"label": "C", "text": "Xe con (B), xe con (A), xe cứu thương đi làm nhiệm vụ cấp cứu."}]'::jsonb, 'A', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (494, 6, 'Thứ tự các xe đi như thế nào là đúng quy tắc giao thông?', '[{"label": "A", "text": "Xe cứu thương đi làm nhiệm vụ cấp cứu, xe chữa cháy đi làm nhiệm vụ chữa cháy, xe con."}, {"label": "B", "text": "Xe chữa cháy đi làm nhiệm vụ chữa cháy, xe cứu thương đi làm nhiệm vụ cấp cứu, xe con."}, {"label": "C", "text": "Xe cứu thương đi làm nhiệm vụ cấp cứu, xe con, xe chữa cháy đi làm nhiệm vụ chữa cháy."}]'::jsonb, 'B', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (495, 6, 'Xe nào được quyền đi trước trong trường hợp này?', '[{"label": "A", "text": "Xe mô tô."}, {"label": "B", "text": "Xe cứu thương đi làm nhiệm vụ cấp cứu."}]'::jsonb, 'B', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (496, 6, 'Theo hướng mũi tên, xe nào phải nhường đường đi cuối cùng qua nơi giao nhau này?', '[{"label": "A", "text": "Xe khách."}, {"label": "B", "text": "Xe tải."}, {"label": "C", "text": "Xe con."}]'::jsonb, 'A', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (497, 6, 'Theo hướng mũi tên, xe nào phải nhường đường là đúng quy tắc giao thông?', '[{"label": "A", "text": "Xe con."}, {"label": "B", "text": "Xe tải."}]'::jsonb, 'A', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (498, 6, 'Xe nào được quyền đi trước trong trường hợp này?', '[{"label": "A", "text": "Xe công an đi làm nhiệm vụ khẩn cấp."}, {"label": "B", "text": "Xe chữa cháy đi làm nhiệm vụ chữa cháy."}]'::jsonb, 'B', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (499, 6, 'Theo tín hiệu đèn, xe nào được phép đi?', '[{"label": "A", "text": "Xe con và xe khách."}, {"label": "B", "text": "Xe mô tô."}]'::jsonb, 'A', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (500, 6, 'Theo tín hiệu đèn, xe nào đi là đúng quy tắc giao thông?', '[{"label": "A", "text": "Xe khách, xe mô tô."}, {"label": "B", "text": "Xe con, xe tải."}, {"label": "C", "text": "Xe tải, xe mô tô."}]'::jsonb, 'B', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (501, 6, 'Trong trường hợp này xe nào được quyền đi trước?', '[{"label": "A", "text": "Xe công an đi làm nhiệm vụ khẩn cấp."}, {"label": "B", "text": "Xe quân sự đi làm nhiệm vụ khẩn cấp."}]'::jsonb, 'B', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (502, 6, 'Theo tín hiệu đèn, xe tải đi theo hướng nào là đúng quy tắc giao thông?', '[{"label": "A", "text": "Hướng 2, 3, 4."}, {"label": "B", "text": "Chỉ hướng 1."}, {"label": "C", "text": "Hướng 1 và 2."}, {"label": "D", "text": "Hướng 3 và 4."}]'::jsonb, 'B', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (503, 6, 'Các xe đi theo hướng mũi tên, những xe nào vi phạm quy tắc giao thông?', '[{"label": "A", "text": "Xe khách, xe tải, xe mô tô."}, {"label": "B", "text": "Xe tải, xe con, xe mô tô."}, {"label": "C", "text": "Xe khách, xe con, xe mô tô."}]'::jsonb, 'A', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (504, 6, 'Theo hướng mũi tên, thứ tự các xe đi như thế nào là đúng quy tắc giao thông?', '[{"label": "A", "text": "Xe khách, xe tải, xe mô tô, xe con."}, {"label": "B", "text": "Xe con, xe khách, xe tải, xe mô tô."}, {"label": "C", "text": "Xe mô tô, xe tải, xe khách, xe con."}, {"label": "D", "text": "Xe mô tô, xe tải, xe con, xe khách."}]'::jsonb, 'C', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (505, 6, 'Trong trường hợp này xe nào đỗ vi phạm quy tắc giao thông?', '[{"label": "A", "text": "Xe tải."}, {"label": "B", "text": "Xe con và mô tô."}, {"label": "C", "text": "Cả ba xe."}, {"label": "D", "text": "Xe con và xe tải."}]'::jsonb, 'A', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (506, 6, 'Theo hướng mũi tên, xe nào được quyền đi trước?', '[{"label": "A", "text": "Xe tải."}, {"label": "B", "text": "Xe con (B)."}, {"label": "C", "text": "Xe con (A)."}]'::jsonb, 'B', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (507, 6, 'Theo hướng mũi tên, những hướng nào xe gắn máy được phép đi?', '[{"label": "A", "text": "Cả ba hướng."}, {"label": "B", "text": "Chỉ hướng 1 và 3."}, {"label": "C", "text": "Chỉ hướng 1."}]'::jsonb, 'A', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (508, 6, 'Xe nào đỗ vi phạm quy tắc giao thông?', '[{"label": "A", "text": "Cả hai xe."}, {"label": "B", "text": "Không xe nào vi phạm."}, {"label": "C", "text": "Chỉ xe mô tô vi phạm."}, {"label": "D", "text": "Chỉ xe tải vi phạm."}]'::jsonb, 'A', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (509, 6, 'Xe nào đỗ vi phạm quy tắc giao thông?', '[{"label": "A", "text": "Chỉ xe mô tô."}, {"label": "B", "text": "Chỉ xe tải."}, {"label": "C", "text": "Cả ba xe."}, {"label": "D", "text": "Chỉ xe mô tô và xe tải."}]'::jsonb, 'C', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (510, 6, 'Xe tải kéo xe mô tô ba bánh như hình này có đúng quy tắc giao thông không?', '[{"label": "A", "text": "Đúng."}, {"label": "B", "text": "Không đúng."}]'::jsonb, 'B', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (511, 6, 'Theo hướng mũi tên, hướng nào xe không được phép đi?', '[{"label": "A", "text": "Hướng 2 và 5."}, {"label": "B", "text": "Chỉ hướng 1."}]'::jsonb, 'B', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (512, 6, 'Theo hướng mũi tên, những hướng nào xe ô tô không được phép đi?', '[{"label": "A", "text": "Hướng 1 và 2."}, {"label": "B", "text": "Hướng 3."}, {"label": "C", "text": "Hướng 1 và 4."}, {"label": "D", "text": "Hướng 2 và 3."}]'::jsonb, 'B', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (513, 6, 'Xe nào vượt đúng quy tắc giao thông?', '[{"label": "A", "text": "Cả hai xe đều đúng."}, {"label": "B", "text": "Xe con."}, {"label": "C", "text": "Xe khách."}]'::jsonb, 'A', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (514, 6, 'Theo hướng mũi tên, gặp biển hướng đi phải theo đặt trước ngã tư, những hướng nào xe được phép đi?', '[{"label": "A", "text": "Hướng 2 và 3."}, {"label": "B", "text": "Hướng 1, 2 và 3."}, {"label": "C", "text": "Hướng 1 và 3."}]'::jsonb, 'A', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (515, 6, 'Xe kéo nhau như hình này có vi phạm quy tắc giao thông không?', '[{"label": "A", "text": "Không."}, {"label": "B", "text": "Vi phạm."}]'::jsonb, 'B', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (516, 6, 'Xe nào phải nhường đường trong trường hợp này?', '[{"label": "A", "text": "Xe khách."}, {"label": "B", "text": "Xe tải."}]'::jsonb, 'A', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (517, 6, 'Xe nào được quyền đi trước trong trường hợp này?', '[{"label": "A", "text": "Xe con."}, {"label": "B", "text": "Xe mô tô."}]'::jsonb, 'B', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (518, 6, 'Xe kéo nhau trong trường hợp này đúng quy định không?', '[{"label": "A", "text": "Không đúng."}, {"label": "B", "text": "Đúng."}]'::jsonb, 'A', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (519, 6, 'Theo hướng mũi tên, những hướng nào xe ô tô con được phép đi?', '[{"label": "A", "text": "Hướng 1."}, {"label": "B", "text": "Hướng 1, 3 và 4."}, {"label": "C", "text": "Hướng 2, 3 và 4."}, {"label": "D", "text": "Cả bốn hướng."}]'::jsonb, 'B', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (520, 6, 'Theo hướng mũi tên, thứ tự các xe đi như thế nào là đúng quy tắc giao thông?', '[{"label": "A", "text": "Xe con (A), xe mô tô, xe con (B), xe đạp."}, {"label": "B", "text": "Xe con (B), xe đạp, xe mô tô, xe con (A)."}, {"label": "C", "text": "Xe con (A), xe con (B), xe mô tô + xe đạp."}, {"label": "D", "text": "Xe mô tô + xe đạp, xe con (A), xe con (B)."}]'::jsonb, 'D', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (521, 6, 'Theo hướng mũi tên, những hướng nào xe tải được phép đi?', '[{"label": "A", "text": "Chỉ hướng 1."}, {"label": "B", "text": "Hướng 1, 3 và 4."}, {"label": "C", "text": "Hướng 1, 2 và 3."}, {"label": "D", "text": "Cả bốn hướng."}]'::jsonb, 'A', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (522, 6, 'Những hướng nào xe tải được phép đi?', '[{"label": "A", "text": "Cả ba hướng."}, {"label": "B", "text": "Hướng 2 và 3."}]'::jsonb, 'B', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (523, 6, 'Những hướng nào xe ô tô tải được phép đi?', '[{"label": "A", "text": "Chỉ hướng 1."}, {"label": "B", "text": "Hướng 1 và 4."}, {"label": "C", "text": "Hướng 1 và 5."}, {"label": "D", "text": "Hướng 1, 4 và 5."}]'::jsonb, 'C', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (524, 6, 'Những hướng nào xe ô tô tải được phép đi?', '[{"label": "A", "text": "Cả bốn hướng."}, {"label": "B", "text": "Trừ hướng 2."}, {"label": "C", "text": "Hướng 2, 3 và 4."}, {"label": "D", "text": "Trừ hướng 4."}]'::jsonb, 'B', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (525, 6, 'Xe nào được quyền đi trước trong trường hợp này?', '[{"label": "A", "text": "Xe mô tô."}, {"label": "B", "text": "Xe con."}]'::jsonb, 'A', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (526, 6, 'Xe ô tô con đi theo chiều mũi tên có vi phạm quy tắc giao thông không?', '[{"label": "A", "text": "Không vi phạm."}, {"label": "B", "text": "Vi phạm."}]'::jsonb, 'B', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (527, 6, 'Xe nào vi phạm quy tắc giao thông?', '[{"label": "A", "text": "Xe khách."}, {"label": "B", "text": "Xe mô tô."}, {"label": "C", "text": "Xe con."}, {"label": "D", "text": "Xe con và xe mô tô."}]'::jsonb, 'C', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (528, 6, 'Các xe đi như thế nào là đúng quy tắc giao thông?', '[{"label": "A", "text": "Các xe ở phía tay phải và tay trái của người điều khiển được phép đi thẳng."}, {"label": "B", "text": "Cho phép các xe ở mọi hướng được phép rẽ phải."}, {"label": "C", "text": "Tất cả các xe phải dừng lại trước ngã tư, trừ những xe đã ở trong ngã tư được phép tiếp tục đi."}]'::jsonb, 'C', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (529, 6, 'Theo hướng mũi tên, xe nào được phép đi?', '[{"label": "A", "text": "Xe mô tô, xe con."}, {"label": "B", "text": "Xe con, xe tải."}, {"label": "C", "text": "Xe mô tô, xe tải."}, {"label": "D", "text": "Cả ba xe."}]'::jsonb, 'C', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (530, 6, 'Xe con vượt xe tải như trường hợp này có đúng không?', '[{"label": "A", "text": "Đúng."}, {"label": "B", "text": "Không đúng."}]'::jsonb, 'A', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (531, 6, 'Xe nào vượt đúng quy tắc giao thông?', '[{"label": "A", "text": "Xe tải."}, {"label": "B", "text": "Cả hai xe."}, {"label": "C", "text": "Xe con."}]'::jsonb, 'C', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (532, 6, 'Đi theo hướng mũi tên, xe nào vi phạm quy tắc giao thông?', '[{"label": "A", "text": "Xe chữa cháy đi làm nhiệm vụ chữa cháy."}, {"label": "B", "text": "Xe tải."}, {"label": "C", "text": "Cả hai xe."}]'::jsonb, 'B', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (533, 6, 'Thứ tự các xe đi như thế nào là đúng quy tắc giao thông?', '[{"label": "A", "text": "Xe khách, xe tải, xe con."}, {"label": "B", "text": "Xe con, xe tải, xe khách."}, {"label": "C", "text": "Xe tải, xe khách, xe con."}]'::jsonb, 'C', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (534, 6, 'Theo hướng mũi tên, thứ tự các xe đi như thế nào là đúng quy tắc giao thông?', '[{"label": "A", "text": "Xe khách và xe tải, xe con."}, {"label": "B", "text": "Xe tải, xe khách, xe con."}, {"label": "C", "text": "Xe con, xe khách, xe tải."}]'::jsonb, 'A', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (535, 6, 'Những hướng nào xe ô tô tải được phép đi?', '[{"label": "A", "text": "Cả bốn hướng."}, {"label": "B", "text": "Chỉ hướng 1 và 2."}, {"label": "C", "text": "Trừ hướng 4."}]'::jsonb, 'C', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (536, 6, 'Theo hướng mũi tên, thứ tự các xe đi như thế nào là đúng?', '[{"label": "A", "text": "Xe công an đi làm nhiệm vụ khẩn cấp, xe con, xe tải, xe khách."}, {"label": "B", "text": "Xe con, xe khách và xe công an đi làm nhiệm vụ khẩn cấp, xe tải."}, {"label": "C", "text": "Xe công an đi làm nhiệm vụ khẩn cấp, xe con, xe khách, xe tải."}, {"label": "D", "text": "Xe con, xe tải, xe khách, xe công an đi làm nhiệm vụ khẩn cấp."}]'::jsonb, 'A', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (537, 6, 'Những hướng nào ô tô tải được phép đi?', '[{"label": "A", "text": "Cả bốn hướng."}, {"label": "B", "text": "Hướng 1, 2 và 3."}, {"label": "C", "text": "Hướng 1 và 4."}, {"label": "D", "text": "Hướng 1, 3 và 4."}]'::jsonb, 'D', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (538, 6, 'Trong hình dưới đây, xe nào chấp hành đúng quy tắc giao thông?', '[{"label": "A", "text": "Chỉ xe khách, xe mô tô."}, {"label": "B", "text": "Tất cả các loại xe trên."}, {"label": "C", "text": "Không xe nào chấp hành đúng quy tắc giao thông."}]'::jsonb, 'B', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (539, 6, 'Theo hướng mũi tên, những hướng nào xe mô tô được phép đi?', '[{"label": "A", "text": "Cả ba hướng."}, {"label": "B", "text": "Hướng 1 và 2."}, {"label": "C", "text": "Hướng 1 và 3."}, {"label": "D", "text": "Hướng 2 và 3."}]'::jsonb, 'C', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (540, 6, 'Trong trường hợp này, thứ tự xe đi như thế nào là đúng quy tắc giao thông?', '[{"label": "A", "text": "Xe công an đi làm nhiệm vụ khẩn cấp, xe quân sự đi làm nhiệm vụ khẩn cấp, xe con + xe mô tô."}, {"label": "B", "text": "Xe quân sự đi làm nhiệm vụ khẩn cấp, xe công an đi làm nhiệm vụ khẩn cấp, xe con + xe mô tô."}, {"label": "C", "text": "Xe mô tô + xe con, xe quân sự đi làm nhiệm vụ khẩn cấp, xe công an đi làm nhiệm vụ khẩn cấp."}]'::jsonb, 'B', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (541, 6, 'Xe nào phải nhường đường là đúng quy tắc giao thông?', '[{"label": "A", "text": "Xe A."}, {"label": "B", "text": "Xe B."}]'::jsonb, 'A', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (542, 6, 'Xe con quay đầu đi ngược lại như hình vẽ dưới có vi phạm quy tắc giao thông không?', '[{"label": "A", "text": "Không vi phạm."}, {"label": "B", "text": "Vi phạm."}]'::jsonb, 'B', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (543, 6, 'Trong hình dưới, những xe nào vi phạm quy tắc giao thông?', '[{"label": "A", "text": "Xe con (E), xe mô tô (C)."}, {"label": "B", "text": "Xe tải (A), xe mô tô (D)."}, {"label": "C", "text": "Xe khách (B), xe mô tô (C)."}, {"label": "D", "text": "Xe khách (B), xe mô tô (D)."}]'::jsonb, 'A', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (544, 6, 'Để điều khiển cho xe đi thẳng, người lái xe phải làm gì là đúng quy tắc giao thông?', '[{"label": "A", "text": "Nhường xe con rẽ trái trước."}, {"label": "B", "text": "Đi thẳng không nhường."}]'::jsonb, 'A', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (545, 6, 'Người lái xe điều khiển xe đi theo hướng nào là đúng quy tắc giao thông?', '[{"label": "A", "text": "Chỉ hướng 2."}, {"label": "B", "text": "Hướng 1 và 2."}, {"label": "C", "text": "Tất cả các hướng trừ hướng 3."}, {"label": "D", "text": "Tất cả các hướng trừ hướng 4."}]'::jsonb, 'B', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (546, 6, 'Xe nào phải nhường đường là đúng quy tắc giao thông?', '[{"label": "A", "text": "Xe con (A)."}, {"label": "B", "text": "Xe con (B)."}]'::jsonb, 'B', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (547, 6, 'Người lái xe điều khiển xe rẽ trái như thế nào là đúng quy tắc giao thông?', '[{"label": "A", "text": "Rẽ trái ngay trước xe buýt."}, {"label": "B", "text": "Rẽ trái trước xe tải."}, {"label": "C", "text": "Nhường đường cho xe buýt và xe tải."}]'::jsonb, 'C', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (548, 6, 'Trong hình dưới, những xe nào vi phạm quy tắc giao thông?', '[{"label": "A", "text": "Xe con (B), xe mô tô (C)."}, {"label": "B", "text": "Xe con (A), xe mô tô (C)."}, {"label": "C", "text": "Xe con (E), xe mô tô (D)."}, {"label": "D", "text": "Tất cả các loại xe trên."}]'::jsonb, 'C', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (549, 6, 'Xe nào đi trước là đúng quy tắc giao thông?', '[{"label": "A", "text": "Xe của bạn."}, {"label": "B", "text": "Xe tải."}]'::jsonb, 'B', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (550, 6, 'Người lái xe có thể quay đầu xe như thế nào là đúng quy tắc giao thông?', '[{"label": "A", "text": "Quay đầu theo hướng A."}, {"label": "B", "text": "Quay đầu theo hướng B."}, {"label": "C", "text": "Cấm quay đầu."}]'::jsonb, 'A', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (551, 6, 'Thứ tự các xe đi như thế nào là đúng quy tắc giao thông?', '[{"label": "A", "text": "Xe con và xe tải, xe của bạn."}, {"label": "B", "text": "Xe của bạn, xe tải, xe con."}, {"label": "C", "text": "Xe của bạn và xe con, xe tải."}, {"label": "D", "text": "Xe của bạn, xe tải + xe con."}]'::jsonb, 'C', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (552, 6, 'Khi muốn vượt xe tải, người lái xe phải làm gì là đúng quy tắc giao thông?', '[{"label": "A", "text": "Tăng tốc cho xe chạy vượt qua."}, {"label": "B", "text": "Bật tín hiệu báo hiệu bằng đèn hoặc còi, khi đủ điều kiện an toàn, tăng tốc cho xe chạy vượt qua."}, {"label": "C", "text": "Đánh lái sang làn bên trái và tăng tốc cho xe chạy vượt qua."}]'::jsonb, 'B', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (553, 6, 'Theo tín hiệu đèn, xe nào phải dừng lại là đúng quy tắc giao thông?', '[{"label": "A", "text": "Xe khách, xe mô tô."}, {"label": "B", "text": "Xe tải, xe mô tô."}, {"label": "C", "text": "Xe con, xe tải."}]'::jsonb, 'C', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (554, 6, 'Trong hình dưới, những xe nào chấp hành quy tắc giao thông?', '[{"label": "A", "text": "Xe tải ( D), xe con (B)."}, {"label": "B", "text": "Xe con (A), xe con (C), xe con (E), xe buýt (G)."}, {"label": "C", "text": "Xe con (A), xe con (B), xe tải (D)."}]'::jsonb, 'B', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (555, 6, 'Người lái xe có được phép vượt xe tải để đi thẳng trong trường hợp này không?', '[{"label": "A", "text": "Được vượt."}, {"label": "B", "text": "Cấm vượt."}]'::jsonb, 'B', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (556, 6, 'Bạn có được phép vượt xe mô tô phía trước không?', '[{"label": "A", "text": "Cho phép."}, {"label": "B", "text": "Không được vượt."}]'::jsonb, 'B', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (557, 6, 'Người lái xe dừng tại vị trí nào là đúng quy tắc giao thông?', '[{"label": "A", "text": "Vị trí A và B."}, {"label": "B", "text": "Vị trí A và C."}, {"label": "C", "text": "Vị trí B và C."}, {"label": "D", "text": "Cả ba vị trí A, B, C."}]'::jsonb, 'C', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (558, 6, 'Bạn được dừng xe ở vị trí nào trong tình huống này?', '[{"label": "A", "text": "Được phép dừng ở vị trí A."}, {"label": "B", "text": "Được phép dừng ở vị trí B."}, {"label": "C", "text": "Được phép dừng ở vị trí A và B."}, {"label": "D", "text": "Không được dừng."}]'::jsonb, 'D', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (559, 6, 'Theo tín hiệu đèn của xe cơ giới, xe nào vi phạm quy tắc giao thông?', '[{"label": "A", "text": "Xe mô tô."}, {"label": "B", "text": "Xe ô tô con."}, {"label": "C", "text": "Không xe nào vi phạm."}, {"label": "D", "text": "Cả hai xe."}]'::jsonb, 'D', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (560, 6, 'Các xe đi theo hướng mũi tên, xe nào vi phạm quy tắc giao thông?', '[{"label": "A", "text": "Xe con."}, {"label": "B", "text": "Xe tải."}, {"label": "C", "text": "Xe con, xe tải."}]'::jsonb, 'B', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (561, 6, 'Các xe đi theo hướng mũi tên, những xe nào vi phạm quy tắc giao thông?', '[{"label": "A", "text": "Xe tải, xe con."}, {"label": "B", "text": "Xe khách, xe con."}, {"label": "C", "text": "Xe khách, xe tải."}]'::jsonb, 'C', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (562, 6, 'Các xe đi theo hướng mũi tên, xe nào vi phạm quy tắc giao thông?', '[{"label": "A", "text": "Xe con, xe tải, xe khách."}, {"label": "B", "text": "Xe tải, xe khách, xe mô tô."}, {"label": "C", "text": "Xe khách, xe mô tô, xe con."}, {"label": "D", "text": "Cả bốn xe."}]'::jsonb, 'B', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (563, 6, 'Các xe đi theo hướng mũi tên, xe nào vi phạm quy tắc giao thông?', '[{"label": "A", "text": "Xe khách, xe tải."}, {"label": "B", "text": "Xe khách, xe con."}, {"label": "C", "text": "Xe con, xe tải."}, {"label": "D", "text": "Xe khách, xe tải, xe con."}]'::jsonb, 'A', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (564, 6, 'Trong tình huống dưới đây, để tránh xe phía trước bị hỏng đột xuất trên đường, người lái xe phải làm gì?', '[{"label": "A", "text": "Đánh lái sang trái cho xe vượt qua."}, {"label": "B", "text": "Quan sát phía trước, phía sau, khi đủ điều kiện an toàn, bật tín hiệu bằng đèn hoặc còi rồi cho xe chạy vượt qua."}, {"label": "C", "text": "Cấm vượt."}]'::jsonb, 'B', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (565, 6, 'Các xe đi theo hướng mũi tên, xe nào chấp hành đúng quy tắc giao thông?', '[{"label": "A", "text": "Xe tải, xe mô tô."}, {"label": "B", "text": "Xe khách, xe mô tô."}, {"label": "C", "text": "Xe tải, xe con."}, {"label": "D", "text": "Xe mô tô, xe con."}]'::jsonb, 'B', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (566, 6, 'Trong hình dưới, những xe nào vi phạm quy tắc giao thông?', '[{"label": "A", "text": "Xe con (A), xe con (B), xe tải (D)."}, {"label": "B", "text": "Xe tải (D), xe con (E), xe buýt (G)."}, {"label": "C", "text": "Xe tải ( D), xe con (B)."}, {"label": "D", "text": "Xe con (B), xe con (C)."}]'::jsonb, 'C', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (567, 6, 'Các xe đi theo thứ tự nào là đúng quy tắc giao thông đường bộ?', '[{"label": "A", "text": "Xe của bạn, xe mô tô, xe con."}, {"label": "B", "text": "Xe con, xe của bạn, xe mô tô."}, {"label": "C", "text": "Xe mô tô, xe con, xe của bạn."}]'::jsonb, 'C', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (568, 6, 'Các xe đi theo thứ tự nào là đúng quy tắc giao thông đường bộ?', '[{"label": "A", "text": "Xe của bạn, xe mô tô, xe con."}, {"label": "B", "text": "Xe con, xe của bạn, xe mô tô."}, {"label": "C", "text": "Xe mô tô, xe con, xe của bạn."}]'::jsonb, 'B', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (569, 6, 'Xe nào phải dừng lại trong trường hợp này?', '[{"label": "A", "text": "Xe con."}, {"label": "B", "text": "Xe của bạn."}, {"label": "C", "text": "Cả hai xe."}]'::jsonb, 'A', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (570, 6, 'Xe của bạn được đi theo hướng nào trong trường hợp này?', '[{"label": "A", "text": "Đi thẳng, rẽ trái."}, {"label": "B", "text": "Đi thẳng, rẽ phải."}, {"label": "C", "text": "Rẽ trái."}, {"label": "D", "text": "Đi thẳng, rẽ phải, rẽ trái."}]'::jsonb, 'A', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (571, 6, 'Xe của bạn được đi theo hướng nào trong trường hợp này?', '[{"label": "A", "text": "Chuyển sang làn đường bên phải và rẽ phải."}, {"label": "B", "text": "Dừng lại trước vạch dừng và rẽ phải khi đèn xanh."}, {"label": "C", "text": "Dừng lại trước vạch dừng và đi thẳng hoặc rẽ trái khi đèn xanh."}]'::jsonb, 'C', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (572, 6, 'Bạn xử lý như thế nào trong trường hợp này?', '[{"label": "A", "text": "Tăng tốc độ, rẽ phải trước xe tải và xe đạp."}, {"label": "B", "text": "Giảm tốc độ, rẽ phải sau xe tải và xe đạp."}, {"label": "C", "text": "Tăng tốc độ, rẽ phải trước xe đạp."}]'::jsonb, 'B', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (573, 6, 'Bạn xử lý như thế nào trong trường hợp này?', '[{"label": "A", "text": "Tăng tốc độ, rẽ phải trước xe con màu xanh phía trước và người đi bộ."}, {"label": "B", "text": "Giảm tốc độ, để người đi bộ qua đường và rẽ phải trước xe con màu xanh."}, {"label": "C", "text": "Giảm tốc độ, để người đi bộ qua đường và rẽ phải sau xe con màu xanh."}]'::jsonb, 'C', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (574, 6, 'Bạn xử lý như thế nào trong trường hợp này?', '[{"label": "A", "text": "Nhường đường cho xe khách và đi trước xe đạp."}, {"label": "B", "text": "Nhường đường cho xe đạp và đi trước xe khách."}, {"label": "C", "text": "Nhường đường cho xe đạp và xe khách."}]'::jsonb, 'C', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (575, 6, 'Xe nào phải nhường đường trong trường hợp này?', '[{"label": "A", "text": "Xe con."}, {"label": "B", "text": "Xe tải."}, {"label": "C", "text": "Xe của bạn."}]'::jsonb, 'B', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (576, 6, 'Xe nào phải nhường đường trong trường hợp này?', '[{"label": "A", "text": "Xe đi ngược chiều."}, {"label": "B", "text": "Xe của bạn."}]'::jsonb, 'B', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (577, 6, 'Bạn xử lý như thế nào khi lái xe ô tô vượt qua đoàn người đi xe đạp có tổ chức?', '[{"label": "A", "text": "Tăng tốc độ, chuyển sang làn đường bên trái để vượt."}, {"label": "B", "text": "Không được vượt những người đi xe đạp."}]'::jsonb, 'B', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (578, 6, 'Phía trước có một xe đang lùi vào nơi đỗ, xe con phía trước đang chuyển sang làn đường bên trái, bạn xử lý như thế nào trong trường hợp này?', '[{"label": "A", "text": "Nếu phía sau không có xe xin vượt, chuyển sang làn đường bên trái."}, {"label": "B", "text": "Nếu phía sau có xe xin vượt, thì giảm tốc độ, ở lại làn đường, dừng lại khi cần thiết."}, {"label": "C", "text": "Tăng tốc độ trên làn đường của mình và vượt xe con."}, {"label": "D", "text": "Ý 1 và ý 2."}]'::jsonb, 'D', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (579, 6, 'Bạn xử lý như thế nào khi xe phía trước đang lùi ra khỏi nơi đỗ?', '[{"label": "A", "text": "Chuyển sang nửa đường bên trái để đi tiếp."}, {"label": "B", "text": "Bấm còi, nháy đèn báo hiệu và đi tiếp."}, {"label": "C", "text": "Giảm tốc độ, dừng lại nhường đường."}]'::jsonb, 'C', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (580, 6, 'Phía trước có một xe màu xanh đang vượt xe màu vàng trên làn đường của bạn, bạn xử lý như thế nào trong trường hợp này?', '[{"label": "A", "text": "Phanh xe giảm tốc độ và đi sát lề đường bên phải."}, {"label": "B", "text": "Bấm còi, nháy đèn báo hiệu, giữ nguyên tốc độ và đi tiếp."}, {"label": "C", "text": "Phanh xe giảm tốc độ và đi sát vào lề đường bên trái."}]'::jsonb, 'A', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (581, 6, 'Xe tải phía trước có tín hiệu xin chuyển làn đường, bạn xử lý như thế nào trong trường hợp này?', '[{"label": "A", "text": "Bật tín hiệu xin chuyển làn đường sang trái để vượt xe tải."}, {"label": "B", "text": "Phanh xe giảm tốc độ chờ xe tải phía trước chuyển làn đường."}, {"label": "C", "text": "Bấm còi báo hiệu và vượt qua xe tải trên làn đường của mình."}]'::jsonb, 'B', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (582, 6, 'Bạn xử lý như thế nào trong trường hợp này?', '[{"label": "A", "text": "Tăng tốc độ và đi thẳng qua ngã tư."}, {"label": "B", "text": "Dừng xe trước vạch dừng."}, {"label": "C", "text": "Giảm tốc độ và đi thẳng qua ngã tư."}]'::jsonb, 'C', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (583, 6, 'Các xe đi theo thứ tự nào là đúng quy tắc giao thông đường bộ?', '[{"label": "A", "text": "Xe của bạn, xe mô tô, xe đạp."}, {"label": "B", "text": "Xe mô tô, xe đạp, xe của bạn."}, {"label": "C", "text": "Xe đạp, xe mô tô, xe của bạn."}]'::jsonb, 'C', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (584, 6, 'Các xe đi theo thứ tự nào là đúng quy tắc giao thông đường bộ?', '[{"label": "A", "text": "Xe của bạn, xe tải, xe con."}, {"label": "B", "text": "Xe con, xe tải, xe của bạn."}, {"label": "C", "text": "Xe tải, xe của bạn, xe con."}, {"label": "D", "text": "Xe của bạn, xe con, xe tải."}]'::jsonb, 'D', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (585, 6, 'Xe nào phải nhường đường trong trường hợp này?', '[{"label": "A", "text": "Xe của bạn."}, {"label": "B", "text": "Xe con."}]'::jsonb, 'A', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (586, 6, 'Xe nào phải nhường đường trong trường hợp này?', '[{"label": "A", "text": "Xe con."}, {"label": "B", "text": "Xe của bạn."}]'::jsonb, 'B', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (587, 6, 'Bạn xử lý như thế nào trong trường hợp này?', '[{"label": "A", "text": "Tăng tốc độ, đi qua vạch người đi bộ sang đường, để người đi bộ sang đường sau."}, {"label": "B", "text": "Giảm tốc độ, đi qua vạch người đi bộ sang đường, để người đi bộ sang đường sau."}, {"label": "C", "text": "Giảm tốc độ, để người đi bộ sang đường trước, sau đó cho xe đi qua vạch người đi bộ sang đường."}]'::jsonb, 'C', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (588, 6, 'Xe nào được đi trước trong trường hợp này?', '[{"label": "A", "text": "Xe con."}, {"label": "B", "text": "Xe của bạn."}]'::jsonb, 'B', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (589, 6, 'Các xe đi theo thứ tự nào là đúng quy tắc giao thông đường bộ?', '[{"label": "A", "text": "Xe con, xe tải, xe của bạn."}, {"label": "B", "text": "Xe tải, xe con, xe của bạn."}, {"label": "C", "text": "Xe tải, xe của bạn, xe con."}]'::jsonb, 'B', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (590, 6, 'Bạn xử lý như thế nào trong trường hợp này?', '[{"label": "A", "text": "Tăng tốc độ cho xe lấn sang phần đường bên trái."}, {"label": "B", "text": "Giảm tốc độ cho xe lấn sang phần đường bên trái."}, {"label": "C", "text": "Giảm tốc độ cho xe đi sát phần đường bên phải."}]'::jsonb, 'C', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (591, 6, 'Xe nào được đi trước trong trường hợp này?', '[{"label": "A", "text": "Xe tải."}, {"label": "B", "text": "Xe của bạn."}]'::jsonb, 'A', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (592, 6, 'Xe nào dừng đúng theo quy tắc giao thông?', '[{"label": "A", "text": "Xe con."}, {"label": "B", "text": "Xe mô tô."}, {"label": "C", "text": "Cả 2 xe đều đúng."}]'::jsonb, 'A', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (593, 6, 'Các xe đi theo thứ tự nào là đúng quy tắc giao thông đường bộ?', '[{"label": "A", "text": "Xe tải, xe đạp, xe của bạn."}, {"label": "B", "text": "Xe của bạn, xe đạp, xe tải."}, {"label": "C", "text": "Xe của bạn, xe tải, xe đạp."}]'::jsonb, 'C', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (594, 6, 'Để điều khiển xe rẽ trái, bạn chọn hướng đi nào dưới đây?', '[{"label": "A", "text": "Hướng 1."}, {"label": "B", "text": "Hướng 2."}, {"label": "C", "text": "Cả hai hướng đều được."}]'::jsonb, 'A', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (595, 6, 'Khi gặp xe ngược chiều bật đèn pha trong tình huống dưới dây, bạn xử lý như thế nào?', '[{"label": "A", "text": "Bật đèn chiếu xa, tăng tốc độ vượt xe cùng chiều."}, {"label": "B", "text": "Giữ nguyên đèn chiếu gần, giảm tốc độ, đi sau xe phía trước."}, {"label": "C", "text": "Giữ nguyên đèn chiếu gần, tăng tốc độ vượt xe cùng chiều."}]'::jsonb, 'B', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (596, 6, 'Xe của bạn đang di chuyển gần đến khu vực giao cắt với đường sắt, khi rào chắn đang dịch chuyển, bạn điều khiển xe như thế nào là đúng quy tắc giao thông?', '[{"label": "A", "text": "Quan sát nếu thấy không có tàu thì tăng tốc cho xe vượt qua đường sắt."}, {"label": "B", "text": "Dừng lại trước rào chắn một khoảng cách an toàn."}, {"label": "C", "text": "Ra tín hiệu, yêu cầu người gác chắn tàu kéo chậm barie để xe bạn qua."}]'::jsonb, 'B', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (597, 6, 'Trong tình huống dưới đây, xe con màu đỏ nhập làn đường cao tốc theo hướng mũi tên là đúng hay sai?', '[{"label": "A", "text": "Đúng."}, {"label": "B", "text": "Sai."}]'::jsonb, 'B', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (598, 6, 'Trong tình huống dưới đây, xe con màu đỏ có được phép vượt khi xe con màu xanh đang vượt xe tải hay không?', '[{"label": "A", "text": "Được vượt."}, {"label": "B", "text": "Không được vượt."}]'::jsonb, 'B', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (599, 6, 'Trong tình huống dưới đây, xe con màu vàng vượt xe con màu đỏ là đúng quy tắc giao thông hay không?', '[{"label": "A", "text": "Đúng."}, {"label": "B", "text": "Không đúng."}]'::jsonb, 'A', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

INSERT INTO questions (id, chapter, question_text, options, correct_answer, is_critical, created_at, updated_at)
VALUES (600, 6, 'Trong tình huống dưới đây, xe đầu kéo kéo rơ moóc (xe container) đang rẽ phải, xe con màu xanh và xe máy phía sau xe container đi như thế nào để bảo đảm an toàn?', '[{"label": "A", "text": "Vượt về phía bên phải để đi tiếp."}, {"label": "B", "text": "Giảm tốc độ chờ xe container rẽ xong rồi tiếp tục đi."}, {"label": "C", "text": "Vượt về phía bên trái để đi tiếp."}]'::jsonb, 'B', FALSE, NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET question_text=EXCLUDED.question_text, options=EXCLUDED.options, correct_answer=EXCLUDED.correct_answer, is_critical=EXCLUDED.is_critical, updated_at=NOW();

