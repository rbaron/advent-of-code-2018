from collections import deque, defaultdict
from functools import cache
import heapq

d = 4002
tx, ty = 5, 746

# d = 510
# tx, ty = 10, 10

m = 20183


# def geo0(y, x):
#     if (y == x == 0) or (y == ty and x == tx):
#         return 0
#     elif y == 0:
#         return 16807 * x
#     elif x == 0:
#         return 48271 * y


# G = [[geo0(y, x) for x in range(tx + 1)] for y in range(ty + 1)]

# for y in range(1, ty + 1):
#     for x in range(1, tx + 1):
#         G[y][x] = ((G[y - 1][x] + d) % m) * ((G[y][x - 1] + d) % m)

# G[ty][tx] = 0


# pt1.
# print(sum((((g + d) % m) % 3) for row in G for g in row))


NEITHER = 0
TORCH = 1
CLIMB = 2


ALLOWED = {
    '.': set([CLIMB, TORCH]),
    '=': set([CLIMB, NEITHER]),
    '|': set([TORCH, NEITHER]),
}

@cache
def alternatives(curr_equip, curr, nb):
    return tuple((eq, 1 if eq == curr_equip else 7 + 1) for eq in ALLOWED[curr] & ALLOWED[nb])


def neighbors(y, x):
    return (
        (y + dy, x + dx)
        for dy, dx in ((-1, 0), (1, 0), (0, -1), (0, 1))
        if (0 <= x + dx) and (0 <= y + dy)
    )


# print(explore(0, 0, TORCH, set()))

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
    return tuple((eq, 1 if eq == curr_equip else 7 + 1) for eq in ALLOWED[curr] & ALLOWED[nb])



def bfs():
    q = deque()
    q.append((0, 0, TORCH, 0))
    added = set([(0, 0, TORCH, 0)])
    seen = dict()
    found_min = float('inf')

    while q:
        y, x, equip, cost = q.popleft()
        # curr = ".=|"[((G(y, x) + d) % m) % 3]

        # if equip not in ALLOWED[curr]:
        #     continue

        if cost > found_min:
            continue

        if y == ty and x == tx:
            found_cost = cost + (0 if equip == TORCH else 7)
            print(f'FOUND! {found_cost=}')
            found_min = min(found_min, found_cost)
            continue 

        if seen.get((y, x, equip), float("inf")) < cost:
            continue
        seen[(y, x, equip)] = cost

        for ny, nx in neighbors(y, x):
            # nb = ".=|"[((G(ny, nx) + d) % m) % 3]
            # for (eq, t) in alternatives(equip, curr, nb):
            for (eq, t) in paths(equip, (y, x), (ny, nx)):
                if (ny, nx, eq, cost + t) not in added:
                    q.append((ny, nx, eq, cost + t))
                    added.add((ny, nx, eq, cost + t))

    return found_min

# Works for the example, but too slow for actual input. 
# print('Found: ', bfs())



def A_star():
    open_set = set([((0, 0), TORCH)])

    # f_score, (y, x), equip
    # open_set = [(0, (0, 0), TORCH)]

    came_from = {}
    g_score = defaultdict(lambda: float('inf'))
    g_score[((0, 0), TORCH)] = 0
    f_score = defaultdict(lambda: float('inf'))
    f_score[((0, 0), TORCH)] = ty + tx

    while open_set:
        f, curr, equip = min((f_score[(n, e)], n, e) for n, e in open_set)
        open_set.remove((curr, equip))
        # print('score:', f, curr, equip, f_score[(0, 0)])

        # Update heap.
        # open_set = [(f_score[pos], pos, equip) for (_, pos, equip) in open_set]
        # heapq.heapify(open_set)
        # f, curr, equip = heapq.heappop(open_set)

        if curr == (ty, tx):
            # path = [(ty, tx)]
            # while curr in came_from:
            #     curr = came_from[curr]
            #     path.append(curr)
            # print(list(reversed(path)))
            return f + (0 if equip == TORCH else 7), g_score[(curr, equip)] + (0 if equip == TORCH else 7)

        for neigh in neighbors(*curr):
            for eq, ncost in paths(equip, curr, neigh):
                n_score = g_score[(curr, equip)] + ncost
                # print('here', equip, curr, neigh, n_score)
                # currc = ".=|"[((G(*curr) + d) % m) % 3]
                # nbc = ".=|"[((G(*neigh) + d) % m) % 3]
                # print(f"Moving from {currc}:{equip} (allowed {ALLOWED[currc]}) to {nbc}:{eq} (allowed {ALLOWED[nbc]}): costs {ncost}")
                if n_score < g_score[(neigh, eq)]:
                    # came_from[neigh] = curr
                    g_score[(neigh, eq)] = n_score
                    f_score[(neigh, eq)] = n_score + abs(ty - neigh[0]) + abs(tx - neigh[1])

                    open_set.add((neigh, eq))
                    # # heapq.heappush(open_set, (f_score[neigh], neigh, eq))
                    # found = False
                    # for i, (hnf, hn, he) in enumerate(open_set):
                    #     if hn == neigh and he == eq:
                    #         open_set[i] = (f_score[neigh], neigh, eq) 
                    #         heapq.heapify(open_set)
                    #         found = True
                    #         break
                    # if not found:
                    #     heapq.heappush(open_set, (f_score[neigh], neigh, eq))

print(A_star())

# pt2. 
# 1025 too low