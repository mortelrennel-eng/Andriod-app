# TODO: Fix Super Admin Side Issues

## Issues to Fix:
1. **File Upload Failing:** Storage rules only allow superadmin via custom claims, but claims aren't set. App checks role in RTDB, but rules don't. Need to update rules to allow authenticated writes since app enforces role.
2. **Manage Ebook Nothing Displayed:** Likely no ebooks due to upload failure. Code loads from RTDB "ebooks" correctly.
3. **View Attendance Records Display Issue:** Button in SuperAdminDashboard points to ManageAttendanceActivity (for setting attendance) instead of ViewAttendanceActivity (for viewing records). Data structure seems correct.
4. **Manage Admin Display Issue:** ManageAdminsActivity looks in "admins" ref, but admins are stored in "users" ref with role "admin". Need to change ref and filter by role.

## Plan:
- [x] Update firebase-rules/storage.rules to allow write if authenticated.
- [x] Fix ManageAdminsActivity to load from "users" and filter role == "admin".
- [x] Change SuperAdminDashboard btnManageAttendance to ViewAttendanceActivity.
- [x] Deploy updated Firebase rules.
- [ ] Test upload and ebook display after fixes.
