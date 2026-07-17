cmake_minimum_required(VERSION 3.22.1)

project(elovaire_chromaprint LANGUAGES C CXX)

option(ELOVAIRE_NATIVE_SANITIZERS "Enable debug native undefined-behavior checks" OFF)

set(CMAKE_CXX_STANDARD 17)
set(CMAKE_CXX_STANDARD_REQUIRED ON)

set(CHROMAPRINT_DIR ${CMAKE_CURRENT_SOURCE_DIR}/chromaprint/src)

add_library(
    elovaire_chromaprint
    SHARED
    chromaprint_jni.cpp
    ${CHROMAPRINT_DIR}/audio_processor.cpp
    ${CHROMAPRINT_DIR}/chroma.cpp
    ${CHROMAPRINT_DIR}/chroma_resampler.cpp
    ${CHROMAPRINT_DIR}/chroma_filter.cpp
    ${CHROMAPRINT_DIR}/spectrum.cpp
    ${CHROMAPRINT_DIR}/fft.cpp
    ${CHROMAPRINT_DIR}/fft_lib_kissfft.cpp
    ${CHROMAPRINT_DIR}/fingerprinter.cpp
    ${CHROMAPRINT_DIR}/image_builder.cpp
    ${CHROMAPRINT_DIR}/simhash.cpp
    ${CHROMAPRINT_DIR}/silence_remover.cpp
    ${CHROMAPRINT_DIR}/fingerprint_calculator.cpp
    ${CHROMAPRINT_DIR}/fingerprint_compressor.cpp
    ${CHROMAPRINT_DIR}/fingerprint_decompressor.cpp
    ${CHROMAPRINT_DIR}/fingerprinter_configuration.cpp
    ${CHROMAPRINT_DIR}/fingerprint_matcher.cpp
    ${CHROMAPRINT_DIR}/chromaprint.cpp
    ${CHROMAPRINT_DIR}/utils/base64.cpp
    ${CHROMAPRINT_DIR}/avresample/resample2.c
    ${CHROMAPRINT_DIR}/3rdparty/kissfft/kiss_fft.c
    ${CHROMAPRINT_DIR}/3rdparty/kissfft/kiss_fftr.c
)

target_include_directories(
    elovaire_chromaprint
    PRIVATE
    ${CMAKE_CURRENT_SOURCE_DIR}
    ${CHROMAPRINT_DIR}
    ${CHROMAPRINT_DIR}/3rdparty/kissfft
)

target_compile_definitions(
    elovaire_chromaprint
    PRIVATE
    HAVE_CONFIG_H
    USE_INTERNAL_AVRESAMPLE
    USE_KISSFFT
    KISS_FFT_USE_ALLOCA=0
    CHROMAPRINT_NODLL
)

target_compile_options(
    elovaire_chromaprint
    PRIVATE
    -Wall
    -Wextra
    -Wno-unused-parameter
    -Werror=format
    -Werror=return-type
    -fstack-protector-strong
    -D_FORTIFY_SOURCE=2
)
target_link_options(
    elovaire_chromaprint
    PRIVATE
    "-Wl,-z,relro"
    "-Wl,-z,now"
    "-Wl,-z,noexecstack"
    "-Wl,-z,max-page-size=16384"
    "-Wl,-z,common-page-size=16384"
)

if(ELOVAIRE_NATIVE_SANITIZERS)
    target_compile_options(
        elovaire_chromaprint
        PRIVATE
        -fsanitize=undefined
        -fno-sanitize-recover=all
        -fno-omit-frame-pointer
    )
    target_link_options(
        elovaire_chromaprint
        PRIVATE
        -fsanitize=undefined
        -fno-sanitize-recover=all
    )
endif()
target_link_libraries(elovaire_chromaprint android log m)
