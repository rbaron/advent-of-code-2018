use std::{
    cmp::Reverse,
    collections::{HashMap, HashSet},
    env::args,
    hash::Hash,
};

#[derive(Debug, PartialEq, Eq, Hash, Clone, Copy)]
enum GroupType {
    Immune,
    Infection,
}

#[derive(Debug, Clone)]
struct Group {
    pub id: usize,
    pub group_type: GroupType,
    pub units: i32,
    pub hit_points: i32,
    pub immunities: HashSet<String>,
    pub weaknesses: HashSet<String>,
    pub attack_damage: i32,
    pub attach_type: String,
    pub initiative: i32,
}

impl Group {
    fn effective_power(&self) -> i32 {
        self.units * self.attack_damage
    }

    fn damage(&self, target: &Group) -> i32 {
        if target.immunities.contains(&self.attach_type) {
            0
        } else if target.weaknesses.contains(&self.attach_type) {
            self.effective_power() * 2
        } else {
            self.effective_power()
        }
    }

    fn kills_units(&self, target: &Group) -> i32 {
        let damage = self.damage(target);
        if damage > 0 {
            (damage / target.hit_points).min(target.units)
        } else {
            0
        }
    }
}

fn parse_group(id: usize, group_type: GroupType, line: &str) -> Result<Group, String> {
    let patt = regex::Regex::new(
        r"(?<units>\d+) units each with (?<hit_points>\d+) hit points (.*)?with an attack that does (?<attack_damage>\d+) (?<attack_type>\w+) damage at initiative (?<initiative>\d+)",
    )
    .unwrap();

    let weak_patt = regex::Regex::new(r"weak to (?<weak>[^;\)]+)").unwrap();
    let imm_patt = regex::Regex::new(r"immune to (?<immune>[^;\)]+)").unwrap();

    match patt.captures(line) {
        Some(caps) => {
            let weak = weak_patt.captures(line);
            let weak: Vec<String> = weak
                .map(|caps| caps["weak"].split(", ").map(String::from).collect())
                .unwrap_or_default();

            let immun = imm_patt.captures(line);
            let immun: Vec<String> = immun
                .map(|caps| caps["immune"].split(", ").map(String::from).collect())
                .unwrap_or_default();

            Ok(Group {
                id,
                group_type,
                units: caps["units"].parse::<i32>().unwrap(),
                hit_points: caps["hit_points"].parse::<i32>().unwrap(),
                immunities: immun.into_iter().collect(),
                weaknesses: weak.into_iter().collect(),
                attack_damage: caps["attack_damage"].parse::<i32>().unwrap(),
                attach_type: caps["attack_type"].to_string(),
                initiative: caps["initiative"].parse::<i32>().unwrap(),
            })
        }
        None => Err(String::from("Unable to parse")),
    }
}

fn target_selection<'a>(groups: &HashMap<usize, Group>) -> HashMap<usize, usize> {
    let mut target_by_attacker = HashMap::new();

    let mut attacker_ids = groups
        .values()
        .map(|g| g.id)
        .filter(|g| groups[g].units > 0)
        .collect::<Vec<_>>();

    attacker_ids.sort_by_key(|g| {
        (
            Reverse(groups[g].effective_power()),
            Reverse(groups[g].initiative),
        )
    });

    let mut selected_targets: HashSet<usize> = HashSet::new();

    for attacker_id in attacker_ids.iter() {
        let attacker = &groups[attacker_id];

        let possible_targets: Vec<&Group> = groups
            .values()
            .filter(|t| {
                t.group_type != attacker.group_type
                    && !selected_targets.contains(&t.id)
                    && attacker.damage(t) > 0
            })
            .collect();

        let target = possible_targets
            .iter()
            .max_by_key(|t| (attacker.damage(t), t.effective_power(), t.initiative));

        if let Some(target) = target {
            target_by_attacker.insert(attacker.id, target.id);
            selected_targets.insert(target.id);

            // println!(
            //     "{:?} would target {:?} (would deal {} damage)",
            //     attacker.global_id(),
            //     target.global_id(),
            //     attacker.damage(target)
            // );
        }
    }

    target_by_attacker
}

fn attack(groups: &mut HashMap<usize, Group>, target_by_group: &HashMap<usize, usize>) -> i32 {
    // Groups with no units cannot attack.
    let mut attacker_ids = groups
        .values()
        .map(|g| g.id)
        .filter(|g| groups[g].units > 0)
        .collect::<Vec<_>>();

    attacker_ids.sort_by_key(|g| Reverse(groups[g].initiative));

    let mut total_units_killed = 0;

    for attacker_id in &attacker_ids {
        if let Some(target_id) = target_by_group.get(&attacker_id) {
            if !groups.contains_key(attacker_id) || !groups.contains_key(target_id) {
                continue;
            }
            let attacker = &groups[attacker_id];
            let target = &groups[&target_by_group[&attacker.id]];

            let kill_units = attacker.kills_units(target);
            let target = groups.get_mut(target_id).unwrap();
            target.units -= kill_units;
            total_units_killed += kill_units;

            let target = &groups[&target_id];
            if target.units <= 0 {
                // Remove dead groups
                groups.retain(|_, g| g.units > 0);
            }
        }
    }
    total_units_killed
}

fn simulate(groups: &Vec<Group>, boost: i32) -> HashMap<GroupType, i32> {
    let mut groups: HashMap<usize, Group> = groups
        .iter()
        .map(|g| {
            let mut g = g.clone();
            if g.group_type == GroupType::Immune {
                g.attack_damage += boost;
            }
            (g.id, g)
        })
        .collect();
    let mut counts = HashMap::new();

    loop {
        let sel = target_selection(&groups);
        let total_units_killed = attack(&mut groups, &sel);
        if total_units_killed == 0 {
            // Stalemate.
            break;
        }
        counts.clear();
        for g in groups.values() {
            if g.units > 0 {
                *counts.entry(g.group_type).or_insert(0) += g.units;
            }
        }
        if counts.len() < 2 {
            break;
        }
    }

    counts
}

fn main() {
    let filename = args().nth(1).expect("Please provide a filename");
    let contents = std::fs::read_to_string(filename).expect("Failed to read file");

    let groups: Vec<&str> = contents.split("\n\n").collect();
    assert_eq!(groups.len(), 2);

    let groups = groups
        .iter()
        .zip(vec![GroupType::Immune, GroupType::Infection])
        .map(|(block, group_type)| {
            block
                .lines()
                .skip(1)
                .enumerate()
                .map(move |(i, l)| (i + 1, group_type, l))
        })
        .flatten()
        .enumerate()
        .map(|(id, (_group_pos, group_type, line))| parse_group(id, group_type, line).unwrap())
        .collect::<Vec<_>>();

    let counts = simulate(&groups, 0);
    println!("Part 1: {:?}", counts);

    let mut lo = 0;
    let mut hi = 10000;
    loop {
        if lo == hi {
            break;
        }
        let boost = (lo + hi) / 2;
        let counts = simulate(&groups, boost);

        // Boost is sufficient.
        if counts.contains_key(&GroupType::Immune) && !counts.contains_key(&GroupType::Infection) {
            hi = boost;
        // Boost is not sufficient.
        } else {
            lo = boost + 1;
        }
    }

    println!(
        "Part 2: Minimum boost: {}, remaining: {:?}",
        lo,
        simulate(&groups, lo)
    );
}
