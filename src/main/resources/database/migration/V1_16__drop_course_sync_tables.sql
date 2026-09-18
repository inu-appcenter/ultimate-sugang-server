-- 강의 동기화 기능 제거(#127). V1_5에서 만든 동기화 이력 테이블 3개를 지운다.
-- 외래 키가 changed_fields -> details -> jobs 방향이라 참조하는 쪽부터 지운다.

-- 수정 항목의 필드별 변경 전후 값
DROP TABLE IF EXISTS course_sync_changed_fields;

-- 작업별 변경 항목
DROP TABLE IF EXISTS course_sync_details;

-- 동기화 작업 이력. admins를 참조하지만 admins는 그대로 둔다
DROP TABLE IF EXISTS course_sync_jobs;
