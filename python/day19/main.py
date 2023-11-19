import fileinput


def run_i(s, pgm):
    try:
        op, *v = pgm[s["ip"]]
    except IndexError:
        return False

    # if op == "#ip":
    #     s["bound"] = v[0]
    #     s["ip"] += 1
    #     return True

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
_, *pgm = pgm
# print(pgm, s)

def decomp(s):
    while True:
        s[2] = 1
        # will loop s[4] times, incrementing s[2] each time
        # r5 is fixed at 0, 1, 2, 3... and incremented once the loop ends
        while True:
            if s[5] * s[2] == s[4]:
                s[0] += s[5]    
            s[2] += 1
            if s[2] > s[4]:
                s[5] += 1
            else:
                continue
            if s[5] > s[4]:
                print('FOUND!!', s[0])
                return
            else:
                break

def decomp2(s):
    # return s[4]
    # return list(i for i in range(s[4]))
    s[0] = sum(i for i in range(1, s[4]+ 1) if s[4] % i == 0)

# Part 2.
s[0] = 1

p = False
while True:
    try:
        op, a, b, o = pgm[s["ip"]]
        if s["ip"] == 1:
            print('istr1', s)
            decomp2(s)
            break
        if not run_i(s, pgm):
            break
    except IndexError:
        break
    # try:
        # op, a, b, o = pgm[s["ip"]]
        # if s['ip'] == 1:
        #     print(s)
        # print(s[5])
        # gtrr 2 4 3
        # if op == 'gtrr' and a == 2 and b == 4 and s[4] == 10551306:
        # if op == 'gtrr' and a == 2 and b == 4:
        #     s[2] = s[4] + 1
        #     # p = True
        #     # print(s[5])
        # if op == 'gtrr' and a == 5 and b == 4 and s[4] == 10551306:
        # if op == 'gtrr' and a == 5 and b == 4:
        #     s[5] = s[4] + 1
        #     s[2] = s[5]
        #     # print(s[5])
        #     # p = True
        # eqrr 3 4 3
        # if op == 'eqrr' and a == 3 and b == 4 and s[4] == 10551306:
        #     s[3] = s[4]
        # gtrr 5 4 3
        # if op == 'gtrr' and a == 5 and b == 4 and s[4] == 10551306:
        #     s[5] = s[4] + 1

    #     if p:
    #         print(f"ip {s['ip']} {[s[i] for i in range(6)]} {pgm[s['ip']]}", end="")
        # if not run_i(s, pgm):
        #     break
    #     if p:
    #         print(f"{[s[i] for i in range(6)]}")
    # except IndexError:
    #     break

print('Finished!', s[0])
