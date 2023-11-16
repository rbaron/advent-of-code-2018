import fileinput
from collections import defaultdict
import re
import sys

sys.setrecursionlimit(10000)

PATT = re.compile("([xy])=(\d+), ([xy])=(\d+)..(\d+)")


def count(y, x, grid, ymax, ymin):
    visited = set()

    def count_inner(y, x):
        if (y, x) in visited:
            return grid[(y, x)], 0

        visited.add((y, x))
        if grid[(y, x)] == "#":
            return "#", 0
        elif y > ymax:
            grid[(y, x)] = "|"
            return "|", 0
        # Down.
        cdown, ndown = count_inner(y + 1, x)
        if cdown == "|":
            grid[(y, x)] = "|"
            return "|", ndown + 1 if y >= ymin else ndown
        cleft, nleft = count_inner(y, x - 1)
        cright, nright = count_inner(y, x + 1)

        # If there's a way left.
        if cleft == "|" and cright != "|":
            # Fix right
            xtmp = x + 1
            while grid[(y, xtmp)] == "~":
                grid[(y, xtmp)] = "|"
                xtmp = xtmp + 1
        elif cright == "|" and cleft != "|":
            # Fix left
            xtmp = x - 1
            while grid[(y, xtmp)] == "~":
                grid[(y, xtmp)] = "|"
                xtmp = xtmp - 1
        grid[(y, x)] = "~" if "|" not in [cleft, cright] else "|"
        return "~" if "|" not in [cleft, cright] else "|", ndown + nleft + nright + 1

    return count_inner(y, x)


def main():
    def parse_line(line):
        c1, n1, c2, c2a, c2b = PATT.findall(line)[0]
        # y, x
        return ((n1, n1), (c2a, c2b)) if c1 == "y" else ((c2a, c2b), (n1, n1))

    grid = defaultdict(lambda: ".")
    grid.update(
        {
            (y, x): "#"
            for (ya, yb), (xa, xb) in [parse_line(l) for l in fileinput.input()]
            for y in range(int(ya), int(yb) + 1)
            for x in range(int(xa), int(xb) + 1)
        }
    )
    ymax = max(y for y, x in grid)
    ymin = min(y for y, x in grid)

    print(f"{ymin=}, {ymax=}")

    print(count(0, 500, grid, ymax, ymin))

    xmax = max(x for y, x in grid)
    xmin = min(x for y, x in grid)

    acc = 0
    for y in range(ymin, ymax + 1):
        for x in range(xmin, xmax + 1):
            # Part 1, has to agree with solution from count().
            # acc = acc + (1 if (grid[(y, x)] in "~|") else 0)
            # Part 2.
            acc = acc + (1 if grid[(y, x)] == "~" else 0)
    print(acc)

    # Print grid.
    # for y in range(ymin - 1, ymax + 2):
    #     for x in range(xmin - 1, xmax + 2):
    #         print(grid[(y, x)], end="")
    #     print()


if __name__ == "__main__":
    main()
