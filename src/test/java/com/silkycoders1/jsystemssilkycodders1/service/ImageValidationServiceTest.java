package com.silkycoders1.jsystemssilkycodders1.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ExtendWith(MockitoExtension.class)
class ImageValidationServiceTest {

    private final ImageValidationService imageValidationService = new ImageValidationService();

    @Test
    void shouldValidateValidJpeg() {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.jpg",
                "image/jpeg",
                new byte[] { (byte) 0xFF, (byte) 0xD8, (byte) 0xFF } // Magic bytes for JPEG
        );
        assertThatCode(() -> imageValidationService.validateImage(file)).doesNotThrowAnyException();
    }

    @Test
    void shouldValidateValidPng() {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.png",
                "image/png",
                new byte[] { (byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A } // Magic bytes for PNG
        );
        assertThatCode(() -> imageValidationService.validateImage(file)).doesNotThrowAnyException();
    }

    @Test
    void shouldValidateValidHeic() {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.heic",
                "image/heic",
                new byte[] {
                        (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x18,
                        (byte) 0x66, (byte) 0x74, (byte) 0x79, (byte) 0x70, // ftyp
                        (byte) 0x68, (byte) 0x65, (byte) 0x69, (byte) 0x63 // heic
                });
        assertThatCode(() -> imageValidationService.validateImage(file)).doesNotThrowAnyException();
    }

    @Test
    void shouldThrowExceptionForTextFile() {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.txt",
                "text/plain",
                "This is a text file".getBytes());
        assertThatThrownBy(() -> imageValidationService.validateImage(file))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldThrowExceptionForFakeImage() {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "fake.jpg",
                "image/jpeg",
                "This is not really an image".getBytes());
        assertThatThrownBy(() -> imageValidationService.validateImage(file))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldThrowExceptionForEmptyFile() {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "empty.jpg",
                "image/jpeg",
                new byte[0]);
        assertThatThrownBy(() -> imageValidationService.validateImage(file))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
