import re, random
dice = re.fullmatch(r"r(\d+)d(\d+)", "r2d100")
if dice:
    num_dice = int(dice.group(1))  # 1
    dice_sides = int(dice.group(2))  # 100
    results = [random.randint(1, dice_sides) for _ in range(num_dice)]
    print(results)