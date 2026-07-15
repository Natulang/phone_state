// swift-tools-version: 5.9

import PackageDescription

let package = Package(
    name: "phone_state",
    platforms: [
        .iOS("13.0")
    ],
    products: [
        .library(name: "phone-state", targets: ["phone_state"])
    ],
    dependencies: [
        .package(name: "FlutterFramework", path: "../FlutterFramework")
    ],
    targets: [
        .target(
            name: "phone_state",
            dependencies: [
                .product(name: "FlutterFramework", package: "FlutterFramework")
            ]
        )
    ]
)
