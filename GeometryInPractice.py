import math

#menu for terminal
def menu():
  print("[1] Calculate the circumference of a circle")
  print("[2] Calculate the area of a piece of land")
  print("[3] Calculate the RPM of a wheel based on vehicle velocity")
  print("[0] Exit")

def get_float(prompt, positive=False):
    while True:
        try:
            val = float(input(prompt))
            if positive and val <= 0:
                print("\nPlease enter a value greater than 0.\n")
                continue
            return val
        except ValueError:
            print("\nInvalid number. Try again.\n")

while True:
    menu()
    choice_str = input("Enter your choice: ").strip()

    if not choice_str.isdigit():
        print("\nInvalid choice. Please enter 0, 1, 2, or 3.\n")
        continue

    choice = int(choice_str)


    if choice ==1:
        #circumference of a circle computation
        radius = float(input("Enter the radius of the circle to calculate the circumference: "))
        C = 2*math.pi*radius
        print("\nthe circumference is:", C, "\n")

    elif choice ==2:
        #area of a piece of land computation
        base = float(input("Enter the base in meters of the land to calculate the area: "))
        height = float(input("Enter the height in meters of the land to calculate the area: "))
        area = base * height
        print(f"\nThe area of the land is: {area} meters squared\n")

    elif choice ==3:
        # calculating the RPM of a wheel
        velocity = float(input("Enter the velocity of the vehicle in meters per second: "))
        wheel_diameter = float(input("Enter the diameter of the wheel in meters: "))
        wheel_radius = wheel_diameter / 2
        circumference_wheel = 2 * math.pi * wheel_radius
        rpm = (velocity / circumference_wheel) * 60
        print("\nThe RPM of the wheel is:", rpm, "\n")

    elif choice ==0:
        print("\nExiting the program.\n")
        break

    else:
        print("\nInvalid choice. Please try again.\n")
