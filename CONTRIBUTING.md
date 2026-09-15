# Quy định Đóng Góp (Contributing Guidelines)

Tài liệu này hướng dẫn cách thức tham gia đóng góp mã nguồn và nâng cấp dự án HaoHanDisplayUI.

---

## 1. Nguyên Tắc Chung (General Rules)

- Không thay đổi hành vi hoặc signature của các public API hiện tại nếu chưa có thảo luận trước.
- Giữ phong cách viết code rõ ràng, tuân thủ tiêu chuẩn mã nguồn Java hiện đại (Java 21).
- Mọi class, record, interface và public method mới trong package `vn.haohan.displayui.api` bắt buộc phải có JavaDoc bằng tiếng Anh đầy đủ `@param`, `@return`, `@throws` (nếu có).


---

## 2. Quy Trình Đóng Góp (Workflow)

1. Fork kho lưu trữ về tài khoản cá nhân.
2. Tạo nhánh mới mô tả đúng tính năng hoặc lỗi cần sửa:
   ```bash
   git checkout -b feature/ten-tinh-nang
   # hoặc
   git checkout -b fix/ten-loi
   ```
3. Viết mã nguồn và bổ sung unit test tương ứng nếu có logic mới.
4. Kiểm tra mã nguồn và biên dịch:
   ```bash
   ./gradlew test
   ./gradlew compileJava
   ```
5. Commit thay đổi với thông điệp rõ ràng theo định dạng Conventional Commits (ví dụ: `feat: ...`, `fix: ...`, `docs: ...`, `refactor: ...`).
6. Đẩy nhánh lên fork và tạo Pull Request (PR) về nhánh chính của kho lưu trữ.

---

## 3. Tiêu Chuẩn Kiểm Thử & Biên Dịch (Testing & Build Standards)

- Mọi Pull Request bắt buộc phải vượt qua toàn bộ unit test (`./gradlew test`) với 0 lỗi.
- Dự án sử dụng toolchain Java 21, đảm bảo không sử dụng các API đã bị deprecate hoặc gỡ bỏ mà không có phương án thay thế an toàn.
- Nếu bổ sung hoặc sửa đổi API công khai, cần cập nhật file `API_DOCUMENTATION.md` đồng bộ.

---

# English Version

This document outlines the guidelines and workflow for contributing to HaoHanDisplayUI.

---

## 1. General Rules

- Do not alter runtime behavior or signatures of existing public APIs without prior discussion.
- Maintain clean, readable code adhering to modern Java standards (Java 21).
- All new public classes, records, interfaces, and methods in the `vn.haohan.displayui.api` package must include comprehensive English JavaDocs with `@param`, `@return`, and `@throws` tags.
- Avoid using emojis in commit messages, technical documentation, or code comments.

---

## 2. Contribution Workflow

1. Fork the repository to your personal account.
2. Create a feature or bugfix branch:
   ```bash
   git checkout -b feature/feature-name
   # or
   git checkout -b fix/bug-name
   ```
3. Implement your changes and add corresponding unit tests where applicable.
4. Run tests and ensure clean compilation:
   ```bash
   ./gradlew test
   ./gradlew compileJava
   ```
5. Commit your changes using Conventional Commit messages (e.g., `feat: ...`, `fix: ...`, `docs: ...`, `refactor: ...`).
6. Push your branch to your fork and submit a Pull Request (PR) targeting the main branch.

---

## 3. Testing & Build Standards

- All Pull Requests must pass the entire test suite (`./gradlew test`) with zero failures.
- The project targets Java 21; ensure no deprecated or removed APIs are used without proper migration paths.
- If public APIs are added or modified, update `API_DOCUMENTATION.md` accordingly.
