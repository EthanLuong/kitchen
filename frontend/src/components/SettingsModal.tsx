import { useState } from "react";
import {
  type UserTypeResponse,
  type UserLocationResponse,
} from "../types/types";

type SettingsModalProps = {
  types: UserTypeResponse[];
  locations: UserLocationResponse[];
  onAddType: (name: string) => Promise<void>;
  onDeleteType: (id: number, name: string) => Promise<void>;
  onAddLocation: (name: string) => Promise<void>;
  onDeleteLocation: (id: number, name: string) => Promise<void>;
  onClose: () => void;
};

export default function SettingsModal({
  types,
  locations,
  onAddType,
  onDeleteType,
  onAddLocation,
  onDeleteLocation,
  onClose,
}: SettingsModalProps) {
  const [newType, setNewType] = useState("");
  const [newLocation, setNewLocation] = useState("");
  const [error, setError] = useState<string | null>(null);
  const [isLoading, setIsLoading] = useState(false);

  async function handleAddType() {
    if (!newType.trim()) return;
    try {
      setIsLoading(true);
      setError(null);
      await onAddType(newType.trim());
      setNewType("");
    } catch (err) {
      setError(err instanceof Error ? err.message : "Something went wrong");
    } finally {
      setIsLoading(false);
    }
  }

  async function handleAddLocation() {
    if (!newLocation.trim()) return;
    try {
      setIsLoading(true);
      setError(null);
      await onAddLocation(newLocation.trim());
      setNewLocation("");
    } catch (err) {
      setError(err instanceof Error ? err.message : "Something went wrong");
    } finally {
      setIsLoading(false);
    }
  }

  async function handleDeleteLocation(id: number, name: string) {
    try {
      setIsLoading(true);
      setError(null);
      await onDeleteLocation(id, name);
    } catch (err) {
      setError(err instanceof Error ? err.message : "Something went wrong");
    } finally {
      setIsLoading(false);
    }
  }

  async function handleDeleteType(id: number, name: string) {
    try {
      setIsLoading(true);
      setError(null);
      await onDeleteType(id, name);
    } catch (err) {
      setError(err instanceof Error ? err.message : "Something went wrong");
    } finally {
      setIsLoading(false);
    }
  }

  return (
    <div className="overlay">
      <div className="modalcard">
        <button className="modal-close" onClick={onClose}>
          ✕
        </button>
        <h2>Settings</h2>

        {error && <p className="form-error">{error}</p>}

        <section className="settings-section">
          <h3>Types</h3>
          <div className="settings-chips">
            {types.map((type) => (
              <div key={type.id} className="settings-chip">
                <span>{type.name}</span>
                <button onClick={() => handleDeleteType(type.id, type.name)}>
                  ✕
                </button>
              </div>
            ))}
          </div>
          <div className="settings-add">
            <input
              type="text"
              value={newType}
              onChange={(e) => setNewType(e.target.value)}
              placeholder="New type..."
            />
            <button onClick={handleAddType} disabled={isLoading}>
              Add
            </button>
          </div>
        </section>

        <section className="settings-section">
          <h3>Locations</h3>
          <div className="settings-chips">
            {locations.map((location) => (
              <div key={location.id} className="settings-chip">
                <span>{location.name}</span>
                <button
                  onClick={() =>
                    handleDeleteLocation(location.id, location.name)
                  }
                >
                  ✕
                </button>
              </div>
            ))}
          </div>
          <div className="settings-add">
            <input
              type="text"
              value={newLocation}
              onChange={(e) => setNewLocation(e.target.value)}
              placeholder="New location..."
            />
            <button onClick={handleAddLocation} disabled={isLoading}>
              Add
            </button>
          </div>
        </section>
      </div>
    </div>
  );
}
