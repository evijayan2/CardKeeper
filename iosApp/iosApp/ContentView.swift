import SwiftUI
import Vision
import Shared
import UIKit

struct ContentView: View {
    @State private var showCamera = false
    @State private var onImageCaptured: ((String, [String]?, String?) -> Void)?

    var body: some View {
        ZStack {
            Color.blue.ignoresSafeArea(.all)
            Text("Loading App...")
                .foregroundColor(.white)
            
            ComposeView(onLaunchCamera: { callback in
                self.onImageCaptured = callback
                self.showCamera = true
            })
            .ignoresSafeArea(.all)
        }
        .sheet(isPresented: $showCamera) {
            CameraView { path, ocrText, barcode in
                self.onImageCaptured?(path, ocrText, barcode)
                self.showCamera = false
            }
        }
    }
}

struct ComposeView: UIViewControllerRepresentable {
    // Callback: Path, OCR Text (Lines), Barcode Raw Data
    let onLaunchCamera: (@escaping (String, [String]?, String?) -> Void) -> Void

    func makeUIViewController(context: Context) -> UIViewController {
        MainViewControllerKt.MainViewController(
            onLaunchCamera: { callback in
                // Bridge Kotlin Callback Interface
                self.onLaunchCamera { path, ocrText, barcode in
                   callback.onScanResult(path: path, ocrText: ocrText, barcode: barcode)
                }
            }
        )
    }

    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {}
}

struct CameraView: UIViewControllerRepresentable {
    let onImageCaptured: (String, [String]?, String?) -> Void

    func makeUIViewController(context: Context) -> UIImagePickerController {
        let picker = UIImagePickerController()
        picker.delegate = context.coordinator
        if UIImagePickerController.isSourceTypeAvailable(.camera) {
            picker.sourceType = .camera
        } else {
            picker.sourceType = .photoLibrary
        }
        return picker
    }

    func updateUIViewController(_ uiViewController: UIImagePickerController, context: Context) {}

    func makeCoordinator() -> Coordinator {
        Coordinator(self)
    }

    class Coordinator: NSObject, UINavigationControllerDelegate, UIImagePickerControllerDelegate {
        let parent: CameraView

        init(_ parent: CameraView) {
            self.parent = parent
        }

        func imagePickerController(_ picker: UIImagePickerController, didFinishPickingMediaWithInfo info: [UIImagePickerController.InfoKey : Any]) {
            if let image = info[.originalImage] as? UIImage {
                // 1. Save Image
                var savedPath = ""
                if let data = image.jpegData(compressionQuality: 0.8) {
                    let filename = UUID().uuidString + ".jpg"
                    let url = FileManager.default.temporaryDirectory.appendingPathComponent(filename)
                    try? data.write(to: url)
                    savedPath = url.path
                }
                
                // 2. Perform OCR & Barcode Scan
                guard let cgImage = image.cgImage else {
                    parent.onImageCaptured(savedPath, nil, nil)
                    picker.dismiss(animated: true)
                    return
                }
                
                let handler = VNImageRequestHandler(cgImage: cgImage, options: [:])
                var ocrText: [String]? = nil
                var barcodeData: String? = nil
                
                // Text Request
                let textRequest = VNRecognizeTextRequest { (request, error) in
                    guard let observations = request.results as? [VNRecognizedTextObservation] else { return }
                    // Filter for high confidence
                    let candidates = observations.compactMap { $0.topCandidates(1).first?.string }
                    if !candidates.isEmpty {
                        ocrText = candidates
                    }
                }
                textRequest.recognitionLevel = .accurate
                
                // Barcode Request
                let barcodeRequest = VNDetectBarcodesRequest { (request, error) in
                     guard let observations = request.results as? [VNBarcodeObservation] else { return }
                     // Prioritize PDF417 for Driver Licenses
                     if let dlBarcode = observations.first(where: { $0.symbology == .pdf417 }) {
                         barcodeData = dlBarcode.payloadStringValue
                     } else {
                         barcodeData = observations.first?.payloadStringValue
                     }
                }
                
                do {
                    try handler.perform([textRequest, barcodeRequest])
                } catch {
                    print("Vision Request Failed: \(error)")
                }
                
                parent.onImageCaptured(savedPath, ocrText, barcodeData)
            }
            picker.dismiss(animated: true)
        }
        
        func imagePickerControllerDidCancel(_ picker: UIImagePickerController) {
             picker.dismiss(animated: true)
        }
    }
}
