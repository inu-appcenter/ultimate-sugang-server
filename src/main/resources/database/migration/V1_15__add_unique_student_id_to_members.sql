-- 로그인 식별자가 이메일에서 학번으로 바뀐다. 학번 하나로 회원을 특정할 수 있어야 한다.
-- 이미 겹치는 학번이 있으면 이 문장이 실패하므로, 배포 전에 중복 행을 확인하고 정리해야 한다.
ALTER TABLE members
    ADD CONSTRAINT uk_student_id UNIQUE (student_id);
