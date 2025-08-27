use std::{
    collections::{HashSet, VecDeque},
    iter::zip,
};

type Point = Vec<i32>;

fn dist(a: &Point, b: &Point) -> i32 {
    zip(a, b).map(|(a1, b1)| (a1 - b1).abs()).sum()
}

fn maybe_merge<'a>(from: &HashSet<&'a Point>, into: &mut HashSet<&'a Point>) -> bool {
    let should_insert = from
        .iter()
        .any(|p1| into.iter().any(|p2| dist(p1, p2) <= 3));

    if should_insert {
        for p1 in from {
            into.insert(p1);
        }
    }
    should_insert
}

fn build_clusters(pts: &Vec<Point>) {
    let mut clusters: VecDeque<HashSet<&Point>> = pts
        .iter()
        .map(|_p: &Vec<i32>| {
            let mut h = HashSet::new();
            h.insert(_p);
            h
        })
        .collect::<VecDeque<_>>();

    let mut count = 0;

    loop {
        if clusters.is_empty() {
            break;
        }

        let c = clusters.pop_front().unwrap();
        let mut merged = false;
        for other in &mut clusters {
            if maybe_merge(&c, other) {
                merged = true;
                break;
            }
        }

        // If not able to merge, we've got a constellation.
        if !merged {
            count += 1;
        }
    }

    println!("{}", count);
}

fn main() {
    let filename = std::env::args().nth(1).unwrap();
    let contents = std::fs::read_to_string(filename).unwrap();
    let pts: Vec<Point> = contents
        .lines()
        .filter_map(|line| {
            let parts = line.split(',');
            Some(parts.map(|p| p.parse::<i32>().unwrap()).collect())
        })
        .collect();

    build_clusters(&pts);
}
