import fileinput


def run_i(s, pgm):
    try:
        op, *v = pgm[s["ip"]]
    except IndexError:
        return False

    if s["bound"] is not None:
        s[s["bound"]] = s["ip"]
    [a, b, o] = v
    match op:
        case "addr":
            s[o] = s[a] + s[b]
        case "addi":
            s[o] = s[a] + b
        case "mulr":
            s[o] = s[a] * s[b]
        case "muli":
            s[o] = s[a] * b
        case "banr":
            s[o] = s[a] & s[b]
        case "bani":
            s[o] = s[a] & b
        case "borr":
            s[o] = s[a] | s[b]
        case "bori":
            s[o] = s[a] | b
        case "setr":
            s[o] = s[a]
        case "seti":
            s[o] = a
        case "gtir":
            s[o] = int(a > s[b])
        case "gtri":
            s[o] = int(s[a] > b)
        case "gtrr":
            s[o] = int(s[a] > s[b])
        case "eqir":
            s[o] = int(a == s[b])
        case "eqri":
            s[o] = int(s[a] == b)
        case "eqrr":
            s[o] = int(s[a] == s[b])
    if s["bound"] is not None:
        s["ip"] = s[s["bound"]]
    s["ip"] += 1
    return True


def parse_line(l):
    op, *v = l.split(" ")
    v = list(map(int, v))
    return [op, v[0]] if op == "#ip" else [op] + v


pgm = [parse_line(l) for l in fileinput.input()]
s = {
    "ip": 0,
    "bound": pgm[0][1],
}
s.update({i: 0 for i in range(6)})

# Discard `#ip x` instruction for convenience.
_, *pgm = pgm


# Manually decompiled from `input-annotated`. Just a partial decompilation of the loops
# that start at address 1, after r4 is set up.
def decomp(s):
    while True:
        s[2] = 1
        # Will loop s[4] times, incrementing s[2] each time
        # r5 is fixed at 0, 1, 2, 3... and incremented once the loop ends
        # r0 += r5 if r4 divisible by r2 and r5
        while True:
            if s[5] * s[2] == s[4]:
                s[0] += s[5]
            s[2] += 1
            if s[2] > s[4]:
                s[5] += 1
            else:
                continue
            if s[5] > s[4]:
                print("FOUND!!", s[0])
                return
            else:
                break


# Optimized version of `decomp`.
def decomp2(s):
    s[0] = sum(i for i in range(1, s[4] + 1) if s[4] % i == 0)


# Part 2.
s[0] = 1

p = False
while True:
    try:
        op, a, b, o = pgm[s["ip"]]
        # Once we jump into the part of the program that has been manually decompiled,
        # we execute our optimized version instead.
        if s["ip"] == 1:
            print("istr1", s)
            decomp2(s)
            break
        if not run_i(s, pgm):
            break
    except IndexError:
        break

print("Finished!", s[0])
