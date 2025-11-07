Smart Kitchen Assistant (C++) — Automated Recipe Adjuster
=========================================================

Features
- Rule-based substitutions (vegan/lactose-free/gluten-free)
- Pantry-aware scaling (limits recipe by available stock)
- Calorie constraint per serving (reduces sugar/fat)
- Servings and pan-size scaling
- Oven sensor offset compensation
- No external dependencies; C++17

Build & Run
-----------
1) Generate build system:
   mkdir build && cd build
   cmake ..

2) Build:
   cmake --build . --config Release

3) Run:
   ./smart_kitchen   (or .\\Release\\smart_kitchen.exe on Windows)

Customize
---------
- Edit `main.cpp` to change pantry and constraints (diet, servings, pan size).
- Extend substitution rules in `assistant.hpp` (vector<SubRule>).
- Add more recipes in `main.cpp` (see `default_cake()`).
