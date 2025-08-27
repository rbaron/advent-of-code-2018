use std::{
    collections::{HashSet, VecDeque},
    iter::zip,
};

type Point = Vec<i32>;

fn dist(a: &Point, b: &Point) -> i32 {
    zip(a, b).map(|(a1, b1)| (a1 - b1).abs()).sum()
}

fn get_median(pts: &Vec<Point>, dim: usize) -> Option<&Point> {
    let mut col = pts.iter().map(|p| p[dim]).collect::<Vec<_>>();
    col.sort();
    let mid = col[col.len() / 2];
    // First point with this median.
    pts.iter().filter(|p| p[dim] == mid).nth(0)
}

#[derive(Debug, Clone, PartialEq, Eq)]
struct KDTree {
    point: Point,
    dim: usize,
    left: Option<Box<KDTree>>,
    right: Option<Box<KDTree>>,
}

fn build_kdtree(pts: &Vec<Point>, dim: usize) -> Option<Box<KDTree>> {
    if pts.len() == 0 {
        return None;
    } else if pts.len() == 1 {
        return Some(Box::new(KDTree {
            point: pts[0].clone(),
            dim: dim,
            left: None,
            right: None,
        }));
    }

    let mid = get_median(pts, dim).unwrap();
    let left_pts = pts
        .iter()
        .filter(|p| *p != mid && p[dim] < mid[dim])
        .map(|p| p.clone())
        .collect::<Vec<_>>();

    let right_pts = pts
        .iter()
        .filter(|p| *p != mid && p[dim] >= mid[dim])
        .map(|p| p.clone())
        .collect::<Vec<_>>();

    Some(Box::new(KDTree {
        point: mid.clone(),
        dim: dim,
        left: build_kdtree(&left_pts, (dim + 1) % 4),
        right: build_kdtree(&right_pts, (dim + 1) % 4),
    }))
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

fn build_clusters(pts: &Vec<Point>, t: Box<KDTree>) {
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

    let t = build_kdtree(&pts, 0);
    build_clusters(&pts, t.unwrap());
}

#[cfg(test)]
mod tests {
    use super::*;

    #[test]
    fn test_median() {
        let pts = vec![
            //
            vec![0, 0, 0, 0],
            vec![2, 3, 4, 3],
            vec![1, 1, 2, 10],
        ];
        assert_eq!(get_median(&pts, 0), Some(&pts[2]));
        assert_eq!(get_median(&pts, 1), Some(&pts[2]));
        assert_eq!(get_median(&pts, 2), Some(&pts[2]));
        assert_eq!(get_median(&pts, 3), Some(&pts[1]));
    }
}
