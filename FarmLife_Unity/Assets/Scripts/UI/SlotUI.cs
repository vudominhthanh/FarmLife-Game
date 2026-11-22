using UnityEngine;
using UnityEngine.UI;
using UnityEngine.EventSystems;
using TMPro;

public class SlotUI : MonoBehaviour, IBeginDragHandler, IDragHandler, IEndDragHandler, IDropHandler
{
    [SerializeField] private Image itemIcon;
    [SerializeField] private TextMeshProUGUI quantityText;
    [SerializeField] private Image highlight;

    private Inventory boundInventory;
    private int slotIndex = -1;
    
    public void Bind(Inventory inventory, int index)
    {
        Unbind();
        boundInventory = inventory;
        slotIndex = index;
        if (boundInventory != null) boundInventory.OnChanged += Refresh;
        Refresh();
    }

    public void SetSelected(bool selected)
    {
        if (highlight != null) highlight.enabled = selected;
    }

    public void Refresh()
    {
        if (boundInventory == null) { SetEmpty(); return; }
        var stack = boundInventory.GetSlot(slotIndex);
        if (stack == null || stack.IsEmpty || stack.Data == null) { SetEmpty(); return; }

        if (itemIcon != null)
        {
            itemIcon.sprite = stack.Data.Icon;
            itemIcon.enabled = true;
            itemIcon.gameObject.SetActive(true);
        }

        if (quantityText != null)
        {
            if (stack.Amount > 1)
            {
                quantityText.text = stack.Amount.ToString();
                quantityText.enabled = true;
                quantityText.gameObject.SetActive(true);
            }
            else
            {
                quantityText.enabled = false;
                quantityText.gameObject.SetActive(false);
            }
        }
    }

    private void SetEmpty()
    {
        if (itemIcon != null)
        {
            itemIcon.sprite = null;
            itemIcon.enabled = false;
            itemIcon.gameObject.SetActive(false);
        }
        if (quantityText != null)
        {
            quantityText.enabled = false;
            quantityText.gameObject.SetActive(false);
        }
    }

    private void OnDestroy() => Unbind();

    private void Unbind()
    {
        if (boundInventory != null) boundInventory.OnChanged -= Refresh;
        boundInventory = null;
    }



    public void OnBeginDrag(PointerEventData eventData)
    {
        var stack = boundInventory.GetSlot(slotIndex);
        if (stack == null || stack.IsEmpty) return;

        if (DragIconUI.Instance == null || DragDropManager.Instance == null)
        {
            return;
        }

        DragIconUI.Instance.Show(stack.Data.Icon);
        DragDropManager.Instance.StartDrag(boundInventory, slotIndex);
    }

    public void OnDrag(PointerEventData eventData)
    {

    }

    public void OnEndDrag(PointerEventData eventData)
    {
        if (DragIconUI.Instance != null && DragDropManager.Instance != null)
        {
            DragIconUI.Instance.Hide();
            DragDropManager.Instance.EndDrag();
        }
    }

    public void OnDrop(PointerEventData eventData)
    {
        if (DragDropManager.Instance.draggedInventory != null)
        {
            DragDropManager.Instance.DropInto(this.boundInventory, this.slotIndex);
        }
    }
}
