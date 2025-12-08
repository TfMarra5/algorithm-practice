import math

"""
1. Convert distances from kilometers to miles
2. Extend it to a scalable unit-conversion system 
(eg, km ↔ miles, Celsius ↔ Fahrenheit, liters ↔ gallons).
"""

def menu():
  print("[1] Convert kilometers to miles")
  print("[2] Convert liters to gallons")
  print("[3] Convert Celsius to Fahrenheit")
  print("[0] Exit")

def get_float(prompt, positive=False):
    while True:
        try:
            val = float(input(prompt))
            if positive and val <= 0:
                print("\n" "Please enter a value greater than 0." "\n")
                continue
            return val
        except ValueError:
            print("\n""Invalid number. Try again." "\n")

while True:
  menu()
  choice_str = input("Enter your choice: ").strip()
  if not choice_str.isdigit():
        print("\n" "Invalid choice. Please enter 0, 1, 2, or 3." "\n") 
        continue
  choice = int(choice_str)
  if choice == 1:
      km = float(input("\n" "Insert distance in kilometers: " "\n"))
      fact = 0.621371
      miles = km * fact
      print("\n" f"{km} kilometers in miles is: {miles:.2f}" "\n")
  elif choice == 2:
      liters = float(input("\n" "Insert volume in liters: " "\n"))
      fact = 0.264172
      gallons = liters * fact
      print(f"{liters} liters in gallons is: {gallons:.2f}" "\n")
  elif choice == 3:
      celsius = float(input("\n" "Insert temperature in Celsius: " "\n"))
      fahrenheit = (celsius * 9/5) + 32
      print("\n" f"{celsius} degrees Celsius in Fahrenheit is: {fahrenheit:.2f}" "\n")
  elif choice == 0:
      print("\n" "Exiting the program." "\n")
      break
  else: 
      print("\n" "Invalid choice. Please try again." "\n")