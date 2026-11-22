using UnityEngine;

public class DragDropManager : MonoBehaviour
{
    public static DragDropManager Instance;

    public Inventory draggedInventory { get; private set; }
    public int draggedIndex { get; private set; }

    private void Awake()
    {
        if (Instance != null && Instance != this) { Destroy(gameObject); return; }
        Instance = this;
    }

    public void StartDrag(Inventory fromInv, int fromIndex)
    {
        draggedInventory = fromInv;
        draggedIndex = fromIndex;
    }

    public void EndDrag()
    {
        draggedInventory = null;
        draggedIndex = -1;
    }

    public void DropInto(Inventory targetInv, int targetIndex)
    {
        if (draggedInventory == null) return;
        if (draggedInventory == targetInv && draggedIndex == targetIndex) return; 

        draggedInventory.MoveTo(targetInv, draggedIndex, targetIndex);
    }
}