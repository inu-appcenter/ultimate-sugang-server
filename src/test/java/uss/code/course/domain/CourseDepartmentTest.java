package uss.code.course.domain;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import uss.code.global.exception.domain.RestApiException;
import uss.code.global.infra.IntegrationTest;
import uss.code.member.domain.MemberDepartment;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static uss.code.global.exception.domain.ExceptionCode.INVALID_DEPARTMENT;
import static uss.code.global.exception.domain.ExceptionCode.INVALID_ENUM_TYPE;
import static uss.code.global.exception.domain.ExceptionCode.INVALID_INTERDISCIPLINARY_DEPARTMENT;

@IntegrationTest
class CourseDepartmentTest {

    @Nested
    class 코드값_변환_테스트 {

        @Test
        void 연계_API의_학과_코드를_상수로_변환한다() {
            //given

            //when & then
            assertThat(CourseDepartment.fromCode("0000077")).isEqualTo(CourseDepartment.COMPUTER_ENGINEERING);
            assertThat(CourseDepartment.fromCode("AIA1")).isEqualTo(CourseDepartment.KOREAN_LITERATURE);
            assertThat(CourseDepartment.fromCode("XAA0")).isEqualTo(CourseDepartment.GENERAL_EDUCATION);
        }

        @Test
        void 이번_학기에_새로_생긴_학과도_변환된다() {
            //given

            //when & then
            assertThat(CourseDepartment.fromCode("0000913")).isEqualTo(CourseDepartment.GLOBAL_TRADE_SERVICE);
            assertThat(CourseDepartment.fromCode("0000912")).isEqualTo(CourseDepartment.INTELLIGENT_ROBOT_SYSTEM);
        }

        @Test
        void 상수명이_바뀌어도_학교_코드는_같은_상수로_변환된다() {
            //given

            //when & then
            assertThat(CourseDepartment.fromCode("0000813")).isEqualTo(CourseDepartment.ELECTRONICS_ENGINEERING_SCHOOL);
            assertThat(CourseDepartment.fromCode("0000156")).isEqualTo(CourseDepartment.CIVIL_ENVIRONMENT_ENGINEERING_MAJOR);
            assertThat(CourseDepartment.fromCode("0000183")).isEqualTo(CourseDepartment.LIFE_SCIENCE_SCHOOL);
            assertThat(CourseDepartment.fromCode("0000184")).isEqualTo(CourseDepartment.LIFE_SCIENCE_MAJOR);
            assertThat(CourseDepartment.fromCode("0000832")).isEqualTo(CourseDepartment.IBE_MAJOR);
        }

        @Test
        void 폐지된_학과와_후신_학부는_서로_다른_코드를_유지한다() {
            //given

            //when & then
            assertThat(CourseDepartment.fromCode("EPC1")).isEqualTo(CourseDepartment.ELECTRONICS_ENGINEERING);
            assertThat(CourseDepartment.fromCode("0000813")).isEqualTo(CourseDepartment.ELECTRONICS_ENGINEERING_SCHOOL);
        }

        @Test
        void 정의되지_않은_코드면_예외가_발생한다() {
            //given
            final String unknownCode = "9999999";

            //when & then
            assertThatThrownBy(() -> CourseDepartment.fromCode(unknownCode))
                    .isInstanceOf(RestApiException.class)
                    .hasFieldOrPropertyWithValue("exceptionCode", INVALID_ENUM_TYPE);
        }

        @Test
        void 빈_코드는_개설되지_않은_학과로_매칭되지_않는다() {
            //given
            // 무역학부, 국제개발협력연계전공은 이번 학기 개설이 없어 code가 빈 문자열이다
            final String blankCode = "";

            //when & then
            assertThatThrownBy(() -> CourseDepartment.fromCode(blankCode))
                    .isInstanceOf(RestApiException.class)
                    .hasFieldOrPropertyWithValue("exceptionCode", INVALID_ENUM_TYPE);
        }

        @Test
        void 코드가_없는_상수도_이름으로는_찾을_수_있다() {
            //given

            //when & then
            assertThat(CourseDepartment.from("TRADE")).isEqualTo(CourseDepartment.TRADE);
            assertThat(CourseDepartment.TRADE.getCode()).isEmpty();
        }
    }

    @Nested
    class 소속_매핑_테스트 {

        @Test
        void 학부_소속에는_학부와_하위_전공이_모두_포함된다() {
            //given
            final MemberDepartment electronicsEngineering = MemberDepartment.ELECTRONICS_ENGINEERING_SCHOOL;

            //when
            final List<CourseDepartment> departments = CourseDepartment.ownedBy(electronicsEngineering);

            //then
            assertThat(departments).containsExactlyInAnyOrder(
                    CourseDepartment.ELECTRONICS_ENGINEERING_SCHOOL,
                    CourseDepartment.ELECTRONICS_ENGINEERING_MAJOR,
                    CourseDepartment.SEMICONDUCTOR_CONVERGENCE_MAJOR,
                    CourseDepartment.ELECTRONICS_ENGINEERING
            );
        }

        @Test
        void 폐지된_학과의_강의는_후신_학부_소속으로_잡힌다() {
            //given

            //when & then
            assertThat(CourseDepartment.ELECTRONICS_ENGINEERING.getOwner())
                    .isEqualTo(MemberDepartment.ELECTRONICS_ENGINEERING_SCHOOL);
            assertThat(CourseDepartment.TRADE.getOwner())
                    .isEqualTo(MemberDepartment.GLOBAL_TRADE_SERVICE);
        }

        @Test
        void 학부와_전공이_이름이_달라도_같은_소속으로_묶인다() {
            //given
            final MemberDepartment urbanEnvironment = MemberDepartment.URBAN_ENVIRONMENT_ENGINEERING_SCHOOL;

            //when
            final List<CourseDepartment> departments = CourseDepartment.ownedBy(urbanEnvironment);

            //then
            assertThat(departments).containsExactlyInAnyOrder(
                    CourseDepartment.URBAN_ENVIRONMENT_ENGINEERING_SCHOOL,
                    CourseDepartment.CIVIL_ENVIRONMENT_ENGINEERING_MAJOR,
                    CourseDepartment.ENVIRONMENT_ENGINEERING_MAJOR
            );
        }

        @Test
        void 학생_소속이_아닌_값은_어느_소속에도_묶이지_않는다() {
            //given

            //when & then
            assertThat(CourseDepartment.GENERAL_EDUCATION.hasOwner()).isFalse();
            assertThat(CourseDepartment.TEACHING.hasOwner()).isFalse();
            assertThat(CourseDepartment.GENERAL_ELECTIVE.hasOwner()).isFalse();
            assertThat(CourseDepartment.MILITARY.hasOwner()).isFalse();
            assertThat(CourseDepartment.FUTURE_AUTOMOBILE.hasOwner()).isFalse();
            assertThat(CourseDepartment.HUSS_OTHER_UNIVERSITY.hasOwner()).isFalse();
        }

        @Test
        void 대응되는_강의_학과가_없는_소속은_빈_목록을_반환한다() {
            //given

            //when & then
            assertThat(CourseDepartment.ownedBy(MemberDepartment.INTERNATIONAL_LIBERAL_ARTS)).isEmpty();
            assertThat(CourseDepartment.ownedBy(MemberDepartment.CONVERGENCE)).isEmpty();
        }

        @Test
        void 동북아_세_전공은_각각_독립된_소속이다() {
            //given

            //when & then
            assertThat(CourseDepartment.ownedBy(MemberDepartment.NORTHEAST_ASIAN_TRADE_MAJOR))
                    .containsExactly(CourseDepartment.NORTHEAST_ASIAN_TRADE_MAJOR);
            assertThat(CourseDepartment.ownedBy(MemberDepartment.IBE_MAJOR))
                    .containsExactly(CourseDepartment.IBE_MAJOR);
        }
    }

    @Nested
    class 연계전공_판정_테스트 {

        @Test
        void 연계전공이면_그대로_반환한다() {
            //given
            final String department = "SOCIAL_DATA_SCIENCE";

            //when
            final CourseDepartment courseDepartment = CourseDepartment.fromInterdisciplinary(department);

            //then
            assertThat(courseDepartment).isEqualTo(CourseDepartment.SOCIAL_DATA_SCIENCE);
        }

        @Test
        void 새로_추가된_지능형로봇시스템도_연계전공으로_인정된다() {
            //given
            final String department = "INTELLIGENT_ROBOT_SYSTEM";

            //when
            final CourseDepartment courseDepartment = CourseDepartment.fromInterdisciplinary(department);

            //then
            assertThat(courseDepartment).isEqualTo(CourseDepartment.INTELLIGENT_ROBOT_SYSTEM);
        }

        @Test
        void 연계전공이_아니면_예외가_발생한다() {
            //given
            final String department = "COMPUTER_ENGINEERING";

            //when & then
            assertThatThrownBy(() -> CourseDepartment.fromInterdisciplinary(department))
                    .isInstanceOf(RestApiException.class)
                    .hasFieldOrPropertyWithValue("exceptionCode", INVALID_INTERDISCIPLINARY_DEPARTMENT);
        }

        @Test
        void 존재하지_않는_학과명이면_예외가_발생한다() {
            //given
            final String department = "UNKNOWN_DEPARTMENT";

            //when & then
            assertThatThrownBy(() -> CourseDepartment.fromInterdisciplinary(department))
                    .isInstanceOf(RestApiException.class)
                    .hasFieldOrPropertyWithValue("exceptionCode", INVALID_ENUM_TYPE);
        }
    }

    @Nested
    class 학과_판정_테스트 {

        @Test
        void 학과_갈래는_실측_드롭다운과_같은_76건이다() {
            //when
            final List<CourseDepartment> departments = CourseDepartment.departmentValues();

            //then
            assertThat(departments).hasSize(76);
        }

        @Test
        void 연계전공_갈래는_실측_드롭다운과_같은_32건이다() {
            //when
            final List<CourseDepartment> interdisciplinary = CourseDepartment.interdisciplinaryValues();

            //then
            assertThat(interdisciplinary).hasSize(32);
        }

        @Test
        void 학과면_그대로_반환한다() {
            //given
            final String department = "COMPUTER_ENGINEERING";

            //when
            final CourseDepartment courseDepartment = CourseDepartment.fromDepartment(department);

            //then
            assertThat(courseDepartment).isEqualTo(CourseDepartment.COMPUTER_ENGINEERING);
        }

        @Test
        void HUSS_두_건은_연계전공이_아니라_학과다() {
            //when & then
            assertThat(CourseDepartment.fromDepartment("HUSS_OTHER_UNIVERSITY"))
                    .isEqualTo(CourseDepartment.HUSS_OTHER_UNIVERSITY);
            assertThat(CourseDepartment.fromDepartment("HUSS_INCLUSIVE_SOCIETY_INITIATIVE"))
                    .isEqualTo(CourseDepartment.HUSS_INCLUSIVE_SOCIETY_INITIATIVE);
        }

        @Test
        void 연계전공을_넘기면_예외가_발생한다() {
            //given
            final String interdisciplinary = "LOGISTICS";

            //when & then
            assertThatThrownBy(() -> CourseDepartment.fromDepartment(interdisciplinary))
                    .isInstanceOf(RestApiException.class)
                    .hasFieldOrPropertyWithValue("exceptionCode", INVALID_DEPARTMENT);
        }

        @Test
        void 교양처럼_학과가_아닌_값을_넘기면_예외가_발생한다() {
            //given
            final String generalEducation = "GENERAL_EDUCATION";

            //when & then
            assertThatThrownBy(() -> CourseDepartment.fromDepartment(generalEducation))
                    .isInstanceOf(RestApiException.class)
                    .hasFieldOrPropertyWithValue("exceptionCode", INVALID_DEPARTMENT);
        }

        @Test
        void 폐지된_학과는_목록에_없지만_소속_매핑에는_남는다() {
            //when & then
            assertThat(CourseDepartment.departmentValues()).doesNotContain(CourseDepartment.TRADE);
            assertThat(CourseDepartment.ownedBy(MemberDepartment.GLOBAL_TRADE_SERVICE))
                    .contains(CourseDepartment.TRADE);
        }
    }

    @Nested
    class 실측_표기_일치_테스트 {

        @Test
        void 쉼표가_들어간_연계전공은_쉼표를_그대로_담는다() {
            //when & then
            assertThat(CourseDepartment.MICE_SPORTS_TOURISM.getName()).isEqualTo("MICE,스포츠및관광연계전공");
            assertThat(CourseDepartment.CLIMATE_ENERGY_ENVIRONMENT.getName()).isEqualTo("기후,에너지및환경연계전공");
        }

        @Test
        void 연계전공의_가운뎃점은_U00B7이다() {
            //given
            final String middleDot = "\u00B7";
            final String hangulLetterAraea = "\u318D";

            //when & then
            assertThat(CourseDepartment.BIO_CONVERGENCE_STARTUP.getName())
                    .isEqualTo("바이오융합" + middleDot + "창업연계전공")
                    .doesNotContain(hangulLetterAraea);
            assertThat(CourseDepartment.AI_STARTUP.getName())
                    .isEqualTo("인공지능" + middleDot + "창업연계전공")
                    .doesNotContain(hangulLetterAraea);
        }

        @Test
        void 야간_학과는_접미를_그대로_담는다() {
            //when & then
            assertThat(CourseDepartment.ECONOMICS_NIGHT.getName()).isEqualTo("경제학과(야)");
            assertThat(CourseDepartment.TRADE_NIGHT.getName()).isEqualTo("무역학부(야)");
            assertThat(CourseDepartment.ECONOMICS_NIGHT.isNight()).isTrue();
        }

        @Test
        void 학사_코드가_없는_신설_항목도_이름으로_찾을_수_있다() {
            //when & then
            assertThat(CourseDepartment.from("ANTIBODY_ENGINEERING").getName()).isEqualTo("항체공학연계전공");
            assertThat(CourseDepartment.ANTIBODY_ENGINEERING.getCode()).isEmpty();
        }
    }
}
