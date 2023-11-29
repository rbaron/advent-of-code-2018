from collections import deque, defaultdict
from functools import cache
import heapq

d = 4002
tx, ty = 5, 746

# d = 510
# tx, ty = 10, 10

m = 20183


NEITHER = 0
TORCH = 1
CLIMB = 2


ALLOWED = {
    ".": set([CLIMB, TORCH]),
    "=": set([CLIMB, NEITHER]),
    "|": set([TORCH, NEITHER]),
}


@cache
def alternatives(curr_equip, curr, nb):
    return tuple(
        (eq, 1 if eq == curr_equip else 7 + 1) for eq in ALLOWED[curr] & ALLOWED[nb]
    )


def neighbors(y, x):
    return (
        (y + dy, x + dx)
        for dy, dx in ((-1, 0), (1, 0), (0, -1), (0, 1))
        if (0 <= x + dx) and (0 <= y + dy)
    )


@cache
def G(y, x):
    if (y == x == 0) or (y == ty and x == tx):
        return 0
    elif y == 0:
        return 16807 * x
    elif x == 0:
        return 48271 * y
    return ((G(y - 1, x) + d) % m) * ((G(y, x - 1) + d) % m)


# Pt 1.
print(sum((((G(y, x) + d) % m) % 3) for y in range(ty + 1) for x in range(tx + 1)))


def print_cave():
    for y in range(ty + 1):
        for x in range(tx + 1):
            if y == x == 0:
                print("M", end="")
            elif y == ty and x == tx:
                print("T", end="")
            else:
                v = ((G(y, x) + d) % m) % 3
                print(".=|"[v], end="")
        print()


# print_cave()


@cache
def paths(curr_equip, curr, nb):
    nb = ".=|"[((G(*nb) + d) % m) % 3]
    curr = ".=|"[((G(*curr) + d) % m) % 3]
    return tuple(
        (eq, 1 if eq == curr_equip else 7 + 1) for eq in ALLOWED[curr] & ALLOWED[nb]
    )


def A_star():
    open_set = set([((0, 0), TORCH)])
    came_from = {}
    g_score = defaultdict(lambda: float("inf"))
    g_score[((0, 0), TORCH)] = 0
    f_score = defaultdict(lambda: float("inf"))
    f_score[((0, 0), TORCH)] = ty + tx

    while open_set:
        f, curr, equip = min((f_score[(n, e)], n, e) for n, e in open_set)
        open_set.remove((curr, equip))

        if curr == (ty, tx):
            return f + (0 if equip == TORCH else 7), g_score[(curr, equip)] + (
                0 if equip == TORCH else 7
            )

        for neigh in neighbors(*curr):
            for eq, ncost in paths(equip, curr, neigh):
                n_score = g_score[(curr, equip)] + ncost
                if n_score < g_score[(neigh, eq)]:
                    g_score[(neigh, eq)] = n_score
                    f_score[(neigh, eq)] = (
                        n_score + abs(ty - neigh[0]) + abs(tx - neigh[1])
                    )
                    open_set.add((neigh, eq))

# Pt 2. Very slow.
print(A_star())
