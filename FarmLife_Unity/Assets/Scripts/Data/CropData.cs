using UnityEngine;

[CreateAssetMenu(fileName = " NewCrop", menuName = "FarmGame/Crop Data")]
public class CropData : ScriptableObject
{
    public ItemData seedItem;
    public ItemData harvestItem;

    [Header("Growth Settings")]
    public float timePerStage = 5f;
    public Sprite[] growthSprites;
    public int yieldAmount = 1;

    public int MaxStages => growthSprites.Length;
}
