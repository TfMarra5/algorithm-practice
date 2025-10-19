import math

"""
Create an algorithm to determine the smallest value among four user-input numbers;
then extend it to find min/max in large datasets;
(streaming data, database queries, or IoT sensors).
"""
start = string = input("Let's start finding the smallest and largest among four numbers? (yes/no): ").strip().lower()
if start != 'yes':
    print("Exiting the program.")
    raise SystemExit

nums = [
    float(input("Enter first number: ")),
    float(input("Enter second number: ")),
    float(input("Enter third number: ")),
    float(input("Enter fourth number: "))
]

smallest = min(nums)
sorted_nums = sorted(nums)

print("\n")
print(f"The smallest number is: {smallest}")
print("\n")
print(f"The numbers in ascending order are: {sorted_nums}")
print("\n")

# Extension to large datasets

mn, mx = float("inf"), float("-inf")

while True:
    s = input("Value (Enter to finish): ").strip()
    if s == "":
        break
    x = float(s)
    if x < mn: mn = x
    if x > mx: mx = x

if mn == float("inf"):
    print("No data.")
else:
    print("min =", mn, " | max =", mx)
