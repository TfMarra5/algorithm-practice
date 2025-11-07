cmake_minimum_required(VERSION 3.10)
project(SmartKitchenAssistant CXX)
set(CMAKE_CXX_STANDARD 17)
set(CMAKE_CXX_STANDARD_REQUIRED ON)

add_executable(smart_kitchen
    src/main.cpp
    src/domain.hpp
    src/assistant.hpp
    src/iot.hpp
)
