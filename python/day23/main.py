from collections import defaultdict
import fileinput
import re
import itertools
import heapq


PATT = re.compile(r"^pos=<(-?\d+),(-?\d+),(-?\d+)>, r=(\d+)$")

pos = [tuple(map(int, PATT.match(line).groups())) for line in fileinput.input()]


def dist(r1, r2):
    return sum(abs(a - b) for a, b in zip(r1, r2))


# Pt 1.
strongest = max(pos, key=lambda p: p[-1])
print(sum(1 for r in pos if dist(r[:-1], strongest[:-1]) <= strongest[-1]))


def coord_range(c1, r1, c2, r2):
    minc, maxc = max(c1 - r1, c2 - r2), min(c1 + r1, c2 + r2)
    return range(minc, maxc + 1)


def split8(cuboid):
    def gen_segments(segment):
        a, b, d = *segment, (segment[1] - segment[0] + 1) // 2
        if a == b:
            yield (a, b)
        else:
            yield from ((a, a + d - 1), ((a + d), b))

    return itertools.product(*(gen_segments(s) for s in cuboid))


def is_unity(cuboid):
    return all(ca == cb for ca, cb in cuboid)


def in_range(cuboid, robot):
    *rob_coords, r = robot

    def axis_dist(coord, seg):
        sega, segb = seg
        return sega - coord if coord < sega else coord - segb if coord > segb else 0

    return sum(axis_dist(c, seg) for c, seg in zip(robot, cuboid)) <= r


def pt2():
    minc = min(c - r for *cs, r in pos for c in cs)
    maxc = max(c + r for *cs, r in pos for c in cs)

    cuboid = ((minc, maxc), (minc, maxc), (minc, maxc))

    # Each heap element is (-1 * n_robots_in_range, cuboid) to keep it sorted by most robots.
    heap = [(-len(pos), cuboid)]

    # n_robots, negative dist_to_origin.
    curr_max = (1, -float("inf"))

    while heap:
        # Get cuboid in max number of robots in range.
        _, c = heapq.heappop(heap)

        n_in_range = sum(1 for r in pos if in_range(c, r))
        if n_in_range < curr_max[0]:
            continue

        if is_unity(c):
            curr_max = max(
                curr_max,
                (n_in_range, -1 * dist((0, 0, 0), (c[0][0], c[1][0], c[2][0]))),
            )
            continue

        for s in split8(c):
            n_in_range = sum(1 for r in pos if in_range(s, r))
            heapq.heappush(heap, (-n_in_range, s))

    n, neg_dist = curr_max
    return -neg_dist


print(pt2())
