import Foundation

private func assertEqual(_ actual: Int, _ expected: Int, _ message: String) {
    if actual != expected {
        fatalError("\(message): expected \(expected), got \(actual)")
    }
}

private func testStartIfNeededPreservesOutgoingStartTime() {
    let baseTime = Date(timeIntervalSince1970: 1_000)
    var currentTime = baseTime
    let tracker = CallDurationTracker(now: { currentTime })

    tracker.start()
    currentTime = baseTime.addingTimeInterval(4)
    tracker.startIfNeeded()
    currentTime = baseTime.addingTimeInterval(7)

    assertEqual(tracker.updateDuration(), 7, "duration should include outgoing dialing time")
}

@main
private struct CallDurationTrackerTestRunner {
    static func main() {
        testStartIfNeededPreservesOutgoingStartTime()
        print("CallDurationTrackerTests passed")
    }
}
