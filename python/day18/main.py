import fileinput
import itertools
from copy import deepcopy


def parse():
    grid = {(y, x): c for y, l in enumerate(fileinput.input()) for x, c in enumerate(l)}
    return grid, max(y for x, y in grid)


def neighbors(y, x, S):
    return (
        (y + dy, x + dx)
        for dy, dx in itertools.product((-1, 0, 1), repeat=2)
        if (not (dx == 0 and dy == 0))
        and x + dx >= 0
        and x + dx < S
        and y + dy >= 0
        and y + dy < S
    )


def evolve(grid, S):
    new_grid = deepcopy(grid)
    for (y, x), c in grid.items():
        match c:
            case ".":
                new_grid[(y, x)] = (
                    "|"
                    if sum(grid[n] == "|" for n in neighbors(y, x, S)) >= 3
                    else grid[(y, x)]
                )
            case "|":
                new_grid[(y, x)] = (
                    "#"
                    if sum(grid[n] == "#" for n in neighbors(y, x, S)) >= 3
                    else grid[(y, x)]
                )
            case "#":
                new_grid[(y, x)] = (
                    "#"
                    if (
                        any(grid[n] == "#" for n in neighbors(y, x, S))
                        and any(grid[n] == "|" for n in neighbors(y, x, S))
                    )
                    else "."
                )
    return new_grid


def print_grid(grid, S):
    for y in range(S):
        for x in range(S):
            print(grid[(y, x)], end="")
        print()


def to_hashable(grid, S):
    return "".join(grid[(y, x)] for y in range(S) for x in range(S))


grid, S = parse()

# Part 1.
# for i in range(10):
#     grid = evolve(grid, S)
# print(sum(v == "|" for v in grid.values()) * sum(v == "#" for v in grid.values()))

# Part 2.
seen_at = dict()
i = 0
while True:
    h = to_hashable(grid, S)
    if h in seen_at:
        print("Found! ", i, seen_at[h], i - seen_at[h])
        break
    seen_at[h] = i
    grid = evolve(grid, S)
    i = i + 1

N = 1_000_000_000
n, r = divmod(N - i, i - seen_at[h])
print(n, r)

for i in range(r):
    grid = evolve(grid, S)

print(sum(v == "|" for v in grid.values()) * sum(v == "#" for v in grid.values()))