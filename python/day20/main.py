from collections import defaultdict

REGEX = open("input").read()


def main():
    s = []
    d = defaultdict(lambda: float("inf"))
    d[(0, 0)] = 0
    s.append((1, 0, (0, 0)))
    while s:
        i, n, p = s.pop()
        c = REGEX[i]
        # print(i, c, n, s)
        d[p] = min(d[p], n)
        if c == "(":
            s.append((i, n, p))
            s.append((i + 1, n, p))
        elif c == "|":
            top_i, top_n, top_p = s[-1]
            assert REGEX[top_i] == "("
            s.append((i + 1, top_n, top_p))
        elif c == ")":
            top_i, top_n, top_p = s.pop()
            assert REGEX[top_i] == "("
            s.append((i + 1, top_n, top_p))
        elif c == "$":
            continue
        elif c in "NEWS":
            py, px = p
            if c == "N":
                py -= 1
            elif c == "S":
                py += 1
            elif c == "W":
                px -= 1
            elif c == "E":
                px += 1
            s.append((i + 1, n + 1, (py, px)))
        else:
            raise ValueError(i, c)

    # pt1.
    # return max(d.values())
    # pt2.
    return sum(1 for v in d.values() if v >= 1000)


print(main())
