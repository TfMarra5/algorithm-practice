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
                print("\n") 
                print("Please enter a value greater than 0.")
                print("\n") 
                continue
            return val
        except ValueError:
            print("\n") 
            print("Invalid number. Try again.")
            print("\n") 

while True:
  menu()
  choice_str = input("Enter your choice: ").strip()
  if not choice_str.isdigit():
        print("\n") 
        print("Invalid choice. Please enter 0, 1, 2, or 3.")
        print("\n") 
        continue
  choice = int(choice_str)
  if choice == 1:
      print("\n")
      km = float(input("Insert distance in kilometers: "))
      print("\n")
      fact = 0.621371
      miles = km * fact
      print("\n")
      print(f"{km} kilometers in miles is: {miles:.2f}")
      print("\n")
  elif choice == 2:
      print("\n")
      liters = float(input("Insert volume in liters: "))
      print("\n")
      fact = 0.264172
      gallons = liters * fact
      print("\n")
      print(f"{liters} liters in gallons is: {gallons:.2f}")
      print("\n")
  elif choice == 3:
      print("\n")
      celsius = float(input("Insert temperature in Celsius: "))
      print("\n")
      fahrenheit = (celsius * 9/5) + 32
      print("\n")
      print(f"{celsius} degrees Celsius in Fahrenheit is: {fahrenheit:.2f}")
      print("\n")
  elif choice == 0:
      print("\n") 
      print("Exiting the program.")
      print("\n") 
      break
  else:
      print("\n") 
      print("Invalid choice. Please try again.")
      print("\n")
