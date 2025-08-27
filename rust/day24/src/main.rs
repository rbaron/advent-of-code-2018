use regex::Regex;
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
    pub group_pos: usize,
    pub units: i32,
    pub hit_points: i32,
    pub immunities: HashSet<String>,
    pub weaknesses: HashSet<String>,
    pub attack_damage: i32,
    pub attach_type: String,
    pub initiative: i32,
}

// Implement Eq for Group
impl Eq for Group {}

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

    fn global_id(&self) -> (GroupType, usize) {
        (self.group_type, self.group_pos)
    }
}

impl PartialEq for Group {
    fn eq(&self, other: &Self) -> bool {
        self.global_id() == other.global_id()
    }
}

impl Hash for Group {
    fn hash<H: std::hash::Hasher>(&self, state: &mut H) {
        self.global_id().hash(state);
    }
}

fn parse_group(
    id: usize,
    group_pos: usize,
    group_type: GroupType,
    line: &str,
) -> Result<Group, String> {
    let patt = regex::Regex::new(
        // r"(?<units>\d+) units each with (?<hit_points>\d+) hit points \((immune to (?<immune>[^;]+))?(; )?(weak to (?<weak>.+))?\) with an attack that does (?<attack_damage>\d+) (?<attack_type>\w+) damage at initiative (?<initiative>\d+)",
        r"(?<units>\d+) units each with (?<hit_points>\d+) hit points (\((immune to (?<immune>[^;]+))?(; )?(weak to (?<weak>.+))?\) )?with an attack that does (?<attack_damage>\d+) (?<attack_type>\w+) damage at initiative (?<initiative>\d+)",
    )
    .unwrap();

    println!("Parsing line: {}", line);

    match patt.captures(line) {
        Some(caps) => {
            // for name in patt.capture_names().flatten() {
            //     if let Some(m) = caps.name(name) {
            //         println!("{} = {}", name, m.as_str());
            //     }
            // }
            Ok(Group {
                id,
                group_type,
                group_pos,
                units: caps["units"].parse::<i32>().unwrap(),
                hit_points: caps["hit_points"].parse::<i32>().unwrap(),
                immunities: match caps.name("immune") {
                    Some(m) => m.as_str().split(", ").map(String::from).collect(),
                    None => HashSet::new(),
                },
                weaknesses: match caps.name("weak") {
                    Some(m) => m.as_str().split(", ").map(String::from).collect(),
                    None => HashSet::new(),
                },
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

    // let mut attackers: Vec<&Group> = groups.values().cloned();
    let mut attackers = groups.values().cloned().collect::<Vec<_>>();
    attackers.sort_by_key(|g| (Reverse(g.effective_power()), Reverse(g.initiative)));

    let mut selected_targets = HashSet::new();

    for attacker in attackers.iter() {
        let possible_targets: Vec<&Group> = attackers
            .iter()
            .filter(|t| t.group_type != attacker.group_type && !selected_targets.contains(&t.id))
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

fn attack(groups: &mut HashMap<usize, Group>, target_by_group: &HashMap<usize, usize>) {
    // Groups with no units cannot attack.
    let mut attacker_ids = groups
        .values()
        .map(|g| g.id)
        .filter(|g| groups[g].units > 0)
        .collect::<Vec<_>>();

    attacker_ids.sort_by_key(|g| Reverse(groups[g].initiative));

    for attacker_id in &attacker_ids {
        if let Some(target_id) = target_by_group.get(&attacker_id) {
            let attacker = &groups[&attacker_id];
            let target = &groups[&target_by_group[&attacker.id]];
            let damage = attacker.damage(target);

            let target = groups.get_mut(target_id).unwrap();
            let kill_units = (damage / target.hit_points).min(target.units);
            target.units -= kill_units;

            let attacker = &groups[&attacker_id];
            let target = &groups[&target_id];

            println!(
                "{:?} attacks {:?} (deals {} damage), killing {} units",
                attacker.global_id(),
                target.global_id(),
                damage,
                kill_units
            );
        }
    }
}

fn main() {
    // println!("Hello, world!");

    let filename = args().nth(1).expect("Please provide a filename");
    let contents = std::fs::read_to_string(filename).expect("Failed to read file");

    // Split on \n\n.
    let groups: Vec<&str> = contents.split("\n\n").collect();

    assert_eq!(groups.len(), 2);

    // let g = parse_group(
    //     GroupType::Immune,
    //     "17 units each with 5390 hit points (weak to radiation, bludgeoning) with an attack that does 4507 fire damage at initiative 2",
    // )
    // .unwrap();

    // let groups = groups
    //     .iter()
    //     .zip(vec![GroupType::Immune, GroupType::Infection])
    //     .map(|(block, group_type)| block.lines().skip(1).map(move |l| (group_type, l)))
    //     .flatten()
    //     .enumerate()
    //     .map(|(id, (group_type, line))| parse_group(id, group_type, line).unwrap())
    //     .collect::<Vec<_>>();

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
        .map(|(id, (group_pos, group_type, line))| {
            parse_group(id, group_pos, group_type, line).unwrap()
        })
        .collect::<Vec<_>>();

    // for g in &groups {
    //     println!("{:?}", g);
    // }
    // return;

    let mut groups: HashMap<usize, Group> = groups.into_iter().map(|g| (g.id, g)).collect();
    let mut counts = HashMap::new();

    loop {
        let sel = target_selection(&groups);
        attack(&mut groups, &sel);
        println!();

        // let groups_by_type = groups.iter().group_by(|(_, g)| g.group_type);
        // let mut counts = HashMap::new();
        counts.clear();
        for g in groups.values() {
            if g.units > 0 {
                *counts.entry(g.group_type).or_insert(0) += g.units;
            }
        }

        // if counts[&GroupType::Immune] == 0 || counts[&GroupType::Infection] == 0 {
        //     break;
        // }
        if counts.len() < 2 {
            break;
        }
    }

    println!("{:?}", counts);

    // 22904 too high
}
