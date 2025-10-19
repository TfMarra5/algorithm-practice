"""
Design an algorithm for a Netflix movie suggestion system using machine learning techniques
(eg, matrix factorization, embeddings, or deep learning).
"""

print("Welcome to the Netflix Movie Suggestion System.")

def menu_netflix():
    print("Choose a genre:")
    print("[1] Action")
    print("[2] Comedy")
    print("[3] Drama")
    print("[4] Horror")
    print("[5] Mistery")
    print("[0] Exit the program")

while True:
    menu_netflix()
    choice_str = input("Enter your choice: ").strip()

    if not choice_str.isdigit():
        print("\n") 
        print("Invalid choice. Please enter 0 - 5.")
        print("\n") 
        continue

    choice = int(choice_str)

    if choice == 1:
        print("\n") 
        print("You selected Action. Suggested movies: Mad Max: Fury Road, John Wick, The Dark Knight.")
        print("\n")

    elif choice == 2:
        print("\n") 
        print("You selected Comedy. Suggested movies: The Hangover, Superbad, Step Brothers.")
        print("\n")
    
    elif choice == 3:
        print("\n") 
        print("You selected Drama. Suggested movies: The Shawshank Redemption, Forrest Gump, The Godfather.")
        print("\n")

    elif choice == 4:
        print("\n") 
        print("You selected Horror. Suggested movies: The Conjuring, Get Out, A Quiet Place.")
        print("\n")

    elif choice == 5:
        print("\n") 
        print("You selected Mistery. Suggested movies: Scooby-Doo, Knives Out, Gone")
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
              