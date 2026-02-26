package com.silkycoders1.jsystemssilkycodders1.tools;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

import static org.assertj.core.api.Assertions.assertThat;

class SinsayToolsTests {

    private SinsayTools sut;

    @BeforeEach
    void setUp() {
        sut = new SinsayTools();
    }

    @Test
    void shouldHaveToolAnnotationOnShowReturnForm() throws NoSuchMethodException {
        Method method = SinsayTools.class.getMethod("showReturnForm", String.class);
        assertThat(method.isAnnotationPresent(Tool.class)).isTrue();
    }

    @Test
    void shouldHaveDescriptionMentioningReturnOrComplaintForm() throws NoSuchMethodException {
        Method method = SinsayTools.class.getMethod("showReturnForm", String.class);
        Tool annotation = method.getAnnotation(Tool.class);
        assertThat(annotation.description()).containsIgnoringCase("return");
        assertThat(annotation.description()).containsIgnoringCase("complaint");
    }

    @Test
    void shouldHaveToolParamAnnotationOnTypeParameter() throws NoSuchMethodException {
        Method method = SinsayTools.class.getMethod("showReturnForm", String.class);
        Parameter param = method.getParameters()[0];
        assertThat(param.isAnnotationPresent(ToolParam.class)).isTrue();
    }

    @Test
    void shouldReturnReturnWhenCalledWithReturn() {
        String result = sut.showReturnForm("return");
        assertThat(result).isEqualTo("return");
    }

    @Test
    void shouldReturnComplaintWhenCalledWithComplaint() {
        String result = sut.showReturnForm("complaint");
        assertThat(result).isEqualTo("complaint");
    }
}
