import React, { useRef, useEffect, useState, forwardRef, useImperativeHandle } from 'react';
import { Camera, CameraOff, RefreshCw } from 'lucide-react';

const WebcamCapture = forwardRef(({ onFrameCapture, isStreaming = true, overlayText }, ref) => {
  const videoRef = useRef(null);
  const canvasRef = useRef(null);
  const [streamActive, setStreamActive] = useState(false);
  const [error, setError] = useState(null);

  useEffect(() => {
    let mediaStream = null;

    const startCamera = async () => {
      try {
        setError(null);
        mediaStream = await navigator.mediaDevices.getUserMedia({
          video: {
            width: { ideal: 1280 },
            height: { ideal: 720 },
            facingMode: 'user',
          },
          audio: false,
        });

        if (videoRef.current) {
          videoRef.current.srcObject = mediaStream;
          videoRef.current.play();
          setStreamActive(true);
        }
      } catch (err) {
        console.error('Erreur accès caméra:', err);
        setError("Impossible d'accéder à la webcam. Veuillez vérifier les permissions de votre navigateur.");
        setStreamActive(false);
      }
    };

    if (isStreaming) {
      startCamera();
    }

    return () => {
      if (mediaStream) {
        mediaStream.getTracks().forEach((track) => track.stop());
      }
    };
  }, [isStreaming]);

  // Expose captureFrame method via ref
  useImperativeHandle(ref, () => ({
    captureBlob: () => {
      return new Promise((resolve) => {
        if (!videoRef.current || !canvasRef.current) {
          resolve(null);
          return;
        }
        const video = videoRef.current;
        const canvas = canvasRef.current;
        canvas.width = video.videoWidth || 640;
        canvas.height = video.videoHeight || 480;
        const ctx = canvas.getContext('2d');
        ctx.drawImage(video, 0, 0, canvas.width, canvas.height);
        canvas.toBlob((blob) => resolve(blob), 'image/jpeg', 0.95);
      });
    },
    captureBase64: () => {
      if (!videoRef.current || !canvasRef.current) return null;
      const video = videoRef.current;
      const canvas = canvasRef.current;
      canvas.width = video.videoWidth || 640;
      canvas.height = video.videoHeight || 480;
      const ctx = canvas.getContext('2d');
      ctx.drawImage(video, 0, 0, canvas.width, canvas.height);
      return canvas.toDataURL('image/jpeg', 0.92);
    }
  }));

  return (
    <div className="relative w-full aspect-video bg-slate-900 rounded-2xl overflow-hidden shadow-xl border border-slate-800 flex items-center justify-center">
      {error ? (
        <div className="text-center p-6 text-red-400">
          <CameraOff className="w-12 h-12 mx-auto mb-3 opacity-80" />
          <p className="text-sm font-medium">{error}</p>
        </div>
      ) : (
        <>
          <video
            ref={videoRef}
            autoPlay
            playsInline
            muted
            className="w-full h-full object-cover transform -scale-x-100"
          />

          {/* Guide de cadrage facial */}
          <div className="absolute inset-0 pointer-events-none flex items-center justify-center">
            <div className="w-64 h-80 border-2 border-dashed border-emerald-400/60 rounded-[48%] shadow-[0_0_30px_rgba(16,185,129,0.2)] flex flex-col justify-between p-4">
              <div className="text-center text-xs font-semibold text-emerald-400 bg-slate-900/80 px-3 py-1 rounded-full backdrop-blur-sm self-center">
                {overlayText || 'Alignez votre visage dans le cadre'}
              </div>
            </div>
          </div>

          {/* Hidden canvas used for frame capture */}
          <canvas ref={canvasRef} className="hidden" />

          {/* Live Indicator */}
          {streamActive && (
            <div className="absolute top-4 left-4 flex items-center gap-2 bg-slate-900/80 backdrop-blur-md px-3 py-1.5 rounded-full border border-slate-700">
              <span className="relative flex h-2.5 w-2.5">
                <span className="animate-ping absolute inline-flex h-full w-full rounded-full bg-emerald-400 opacity-75"></span>
                <span className="relative inline-flex rounded-full h-2.5 w-2.5 bg-emerald-500"></span>
              </span>
              <span className="text-xs font-semibold text-white uppercase tracking-wider">WebRTC Live</span>
            </div>
          )}
        </>
      )}
    </div>
  );
});

export default WebcamCapture;
